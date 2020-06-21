package me.saket.press.shared.sync.git

import com.benasher44.uuid.uuidFrom
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.home.SplitHeadingAndBody
import me.saket.press.shared.sync.git.FileNameSanitizer.sanitize

typealias FileName = String

//private inline class Record(val filename: FileName) {
//  companion object {
//    private const val SEPARATOR = "___"
//  }
//
//  val noteName: String get() = filename.substringBefore(SEPARATOR)
//  val noteId: String get() = filename.substringAfter(SEPARATOR)
//}

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
    private const val SEPARATOR = "___"
  }

  private val registerDirectory = File(directory, ".press/registers")

  private fun serialize(noteFileName: String, id: NoteId) =
    "$noteFileName$SEPARATOR${id.value}"

  private fun deserialize(registerName: String): Pair<String, String> {
    val (name, id) = registerName.split(SEPARATOR)
    return name to id
  }

  /**
   * todo: accept a file instead.
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
      val (name, id) = deserialize(registerName = file.name)
      if (name == fileName) {
        return NoteId(uuidFrom(id))
      }
    }
    return null
  }

  fun recordNewNoteId(notesDirectory: File, noteFile: File, id: NoteId) {
    require(noteFile.extension == "md")
    require(!noteFile.relativePathIn(notesDirectory).contains("/"))

    if (!registerDirectory.exists) {
      registerDirectory.makeDirectory(recursively = true)
    }
    val serializedName = serialize(noteFile.nameWithoutExtension, id)
    File(registerDirectory, serializedName).write("")
  }

  fun fileFor(directory: File, note: Note): File {
    val (heading) = SplitHeadingAndBody.split(note.content)
    val expectedName = if (heading.isNotBlank()) heading else "untitled_note"

    // Suffix the name to avoid conflicts. E.g., "untitled_note_2".
    var uniqueName: String
    var conflicts = 0
    loop@ while (true) {
      uniqueName = sanitize(expectedName, MAX_NAME_LENGTH) + if (conflicts++ == 0) "" else "_$conflicts"
      when (noteIdFor("$uniqueName.md")) {
        note.uuid -> break@loop   // Reuse the same name.
        null -> break@loop        // New note. Can still use this name.
        else -> continue@loop     // Conflict! Try another name.
      }
    }

    return File(directory, "$uniqueName.md").also {
      recordNewNoteId(directory, it, note.uuid)
    }
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

    val noteIds = latestNotes.map { it.uuid.value.toString() }

    for (file in registerDirectory.children().reversed()) {
      val (_, id) = deserialize(registerName = file.name)
      if (id !in noteIds) {
        file.delete()
      }
    }
  }
}
