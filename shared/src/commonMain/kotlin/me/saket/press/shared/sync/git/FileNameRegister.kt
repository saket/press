package me.saket.press.shared.sync.git

import com.benasher44.uuid.uuidFrom
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.home.SplitHeadingAndBody
import me.saket.press.shared.sync.git.FileNameSanitizer.sanitize

typealias FileName = String

/**
 * Press tries really hard to avoid leaking Press's implementation into user's git repository.
 * This includes using human readable filenames instead of UUIDs, generated from note titles.
 * An unfortunate drawback is that notes with the same title (e.g., "shopping checklist")
 * can't have the same filename. This class maintains a mapping of filenames to their UUIDs
 * to solve this.
 *
 * At the time of writing this, I'm really hoping this works out alright. Otherwise, Press
 * will have to start suffixing filenames with 32 character long UUIDs 🤮.
 */
@OptIn(ExperimentalStdlibApi::class)
class FileNameRegister(directory: File) {
  companion object {
    // Both NTFS and Unix file systems have a max-length of 255 letters.
    // Reserving 15 letters for handling conflicts and file name extension.
    private const val MAX_NAME_LENGTH = 240
  }

  private val registerDirectory = File(directory, ".press/registers").also {
    it.makeDirectory(recursively = true)
  }

  /**
   * If a mapping does not exist, this file is either new or this register
   * was recreated after getting deleted. In both cases, a new ID should be
   * created.
   */
  @Suppress("NAME_SHADOWING")
  fun noteIdFor(fileName: FileName): NoteId? {
    require(fileName.endsWith("md")) { "Not a note: $fileName" }

    if (!registerDirectory.exists) {
      // Likely checking out a remote commit that
      // doesn't have the registers directory yet.
      return null
    }

    val fileName = fileName.substringBeforeLast(".")
    for (file in registerDirectory.children()) {
      val record = Record(registerName = file.name)
      if (record.noteName == fileName) {
        return NoteId(uuidFrom(record.noteId))
      }
    }
    return null
  }

  @Suppress("CascadeIf")
  fun fileFor(notesDirectory: File, note: Note): File {
    val oldRecord = existingRecordFor(note)
    val oldNoteFile = oldRecord?.file(notesDirectory)?.existsOrNull()

    val newNoteName = oldNoteFile.hideAndRun {
      // The old file (if any) needs to be hidden or else it'll
      // be seen as a conflict when a new name is generated.
      generateFileNameFor(note)
    }

    return if (oldNoteFile == null) {
      // New note. A new file needs to be created.
      File(notesDirectory, newNoteName).also {
        recordFileForNote(notesDirectory, it, note.id)
      }
    } else if (oldNoteFile.name == newNoteName) {
      // A file already exists and the name matches the note's heading.
      oldNoteFile
    } else {
      // A file already exists, but the heading was changed. Rename the file.
      oldNoteFile.renameTo(newNoteName).also {
        println("Renaming to $newNoteName")
        File(registerDirectory, oldRecord.registerName).delete()
        recordFileForNote(notesDirectory, it, note.id)
      }
    }
  }

  private inline fun <T> File?.hideAndRun(crossinline run: () -> T): T {
    return if (this == null) run()
    else {
      val origName = name
      val renamedFile = renameTo("__temp")
      val value = run()
      renamedFile.renameTo(origName)
      value
    }
  }

  fun recordFileForNote(notesDirectory: File, noteFile: File, id: NoteId) {
    require(noteFile.extension == "md")
    require(!noteFile.relativePathIn(notesDirectory).contains("/"))

    if (!registerDirectory.exists) {
      registerDirectory.makeDirectory(recursively = true)
    }
    val serializedName = Record.serialize(noteFile.nameWithoutExtension, id)
    File(registerDirectory, serializedName).write("")
  }

  private fun existingRecordFor(note: Note): Record? {
    val noteIdString = note.id.value.toString()

    for (file in registerDirectory.children()) {
      val record = Record(registerName = file.name)
      if (record.noteId == noteIdString) {
        return record
      }
    }
    return null
  }

  private fun generateFileNameFor(note: Note): String {
    val (heading) = SplitHeadingAndBody.split(note.content)
    val expectedName = if (heading.isNotBlank()) heading else "untitled_note"

    // Suffix the name to avoid conflicts. E.g., "untitled_note_2".
    var uniqueName: String
    var conflicts = 0
    loop@ while (true) {
      uniqueName = sanitize(expectedName, MAX_NAME_LENGTH) + if (conflicts++ == 0) "" else "_$conflicts"
      when (noteIdFor("$uniqueName.md")) {
        note.id -> break@loop     // Reuse the same name.
        null -> break@loop        // New note. Can still use this name.
        else -> continue@loop     // Conflict! Try another name.
      }
    }

    return "$uniqueName.md"
  }

  fun findNewNameOnConflict(noteFile: File): FileName {
    val extension = noteFile.extension
    val conflictedName = noteFile.nameWithoutExtension
    val existingNames = noteFile.parent!!.children().map { it.name }

    // Suffix the name to avoid conflicts. E.g., "untitled_note_2".
    var conflicts = 0
    var newName: String = noteFile.name
    while (newName in existingNames) {
      newName = "${sanitize(conflictedName, MAX_NAME_LENGTH)}_${++conflicts + 1}.$extension"
    }
    return newName
  }

  fun pruneStaleRecords(latestNotes: List<Note>) {
    if (!registerDirectory.exists) return

    val noteIds = latestNotes.map { it.id.value.toString() }
    for (file in registerDirectory.children().reversed()) {
      val record = Record(registerName = file.name)
      if (record.noteId !in noteIds) {
        file.delete()
      }
    }
  }
}

private fun File.existsOrNull(): File? {
  return if (exists) this else null
}

private inline class Record(val registerName: FileName) {
  companion object {
    private const val SEPARATOR = "___"

    fun serialize(noteFileName: String, id: NoteId) =
      "$noteFileName${SEPARATOR}${id.value}"
  }

  val noteName: String get() = registerName.substringBefore(SEPARATOR)
  val noteId: String get() = registerName.substringAfter(SEPARATOR)
  fun file(directory: File): File = File(directory, "$noteName.md")
}
