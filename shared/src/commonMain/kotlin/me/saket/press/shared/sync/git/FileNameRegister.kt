package me.saket.press.shared.sync.git

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
internal class FileNameRegister(private val notesDirectory: File) {
  companion object {
    // Both NTFS and Unix file systems have a max-length of 255 letters.
    // Reserving 15 letters for handling conflicts and file name extension.
    private const val MAX_NAME_LENGTH = 240
  }

  private val registerDirectory = File(notesDirectory, ".press/registers").also {
    it.makeDirectory(recursively = true)
  }

  /**
   * @param fileName name relative to the notes directory.
   *
   * If a mapping does not exist, this file is either new or this register
   * was recreated after getting deleted. In both cases, a new ID should be
   * created.
   */
  @Suppress("NAME_SHADOWING")
  fun recordFor(relativePath: String): Record? {
    require(relativePath.endsWith("md")) { "Not a note: $relativePath" }
    require(!relativePath.hasMultipleOf('/')) { "Nested folders aren't supported yet" }

    // if (!registerDirectory.exists) {
    //   // Likely checking out a remote commit that
    //   // doesn't have the registers directory yet.
    //   return null
    // }

    // Example: "archived/uncharted.md"
    val fileName = relativePath.substringBeforeLast(".").substringAfterLast("/")    // e.g., "uncharted"
    val folderName = relativePath.substringBefore("/", missingDelimiterValue = "")  // e.g., "archived"

    for (file in allRegisterFiles(folderName)) {
      val record = Record.from(registerDirectory, file)
      if (record.noteFileName == fileName) {
        return record.also {
          println("Record for $relativePath is $record")
        }
      }
    }
    return null
  }

  fun recordFor(relativePath: FileName, oldPath: String?): Record? {
    val oldRecord = oldPath?.let { recordFor(it) }

    return if (oldRecord == null) {
      recordFor(relativePath)
    } else {
      oldRecord.registerFileIn(registerDirectory).delete()
      createNewRecordFor(
          noteFile = File(notesDirectory, relativePath),
          id = oldRecord.noteId
      )
    }
  }

  private fun recordFor(note: Note): Record? {
    val noteId = note.id.value.toString()

    for (registerFile in allRegisterFiles()) {
      val record = Record.from(registerDirectory, registerFile)
      if (record.noteIdString == noteId) {
        return record
      }
    }
    return null
  }

  fun noteIdFor(relativePath: String): NoteId? {
    return recordFor(relativePath)?.noteId
  }

  // todo: use functional interface when kotlin 1.4 is released.
  interface OnRenameListener {
    fun onRename(oldName: String, newName: String)
  }

  @Suppress("CascadeIf")
  fun fileFor(note: Note, renameListener: OnRenameListener? = null): File {
    val oldRecord = recordFor(note)
    val oldNoteFile = oldRecord?.noteFileIn(notesDirectory).existsOrNull()

    val newNoteName = oldNoteFile.hideAndRun {
      // The old file (if any) needs to be hidden or else it'll
      // be seen as a conflict when a new name is generated.
      generateFileNameFor(note)
    }

    return if (oldNoteFile == null) {
      // New note. A new file needs to be created.
      File(notesDirectory, newNoteName).also {
        createNewRecordFor(it, note.id)
      }
    } else if (oldNoteFile.relativePathIn(notesDirectory) == newNoteName) {
      // A file already exists and the name matches the note's heading.
      oldNoteFile
    } else {
      // A file already exists, but the heading was changed. Rename the file.
      oldNoteFile.renameTo(File(notesDirectory, newNoteName)).also {
        println("Deleting $oldRecord")
        oldRecord!!.registerFileIn(registerDirectory).delete()
        createNewRecordFor(it, note.id)
        renameListener?.onRename(oldName = oldNoteFile.name, newName = newNoteName)
      }
    }
  }

  fun createNewRecordFor(noteFile: File, id: NoteId): Record {
    require(noteFile.extension == "md")

    if (!registerDirectory.exists) {
      registerDirectory.makeDirectory(recursively = true)
    }
    val recordFile = Record.writeToFile(registerDirectory, notesDirectory, noteFile, id)
    println("Creating record ${recordFile.name}")
    return Record.from(registerDirectory, recordFile)
  }

  private fun allRegisterFiles(folder: String = ""): List<File> =
    with(File(registerDirectory, folder)) {
      return when {
        !exists -> emptyList()
        else -> children(recursively = true).filter { !it.isDirectory }
      }
    }

  private fun generateFileNameFor(note: Note): String {
    val (heading) = SplitHeadingAndBody.split(note.content)
    val expectedName = if (heading.isNotBlank()) heading else "untitled_note"
    val folder = note.folder()?.plus("/") ?: ""

    // Suffix the name to avoid conflicts. E.g., "untitled_note_2".
    var uniqueName: String
    var conflicts = 0
    loop@ while (true) {
      val conflictSuffix = if (conflicts++ == 0) "" else "_$conflicts"
      uniqueName = "$folder${sanitize(expectedName, MAX_NAME_LENGTH)}$conflictSuffix.md"

      when (noteIdFor(uniqueName)) {
        note.id -> break@loop     // Reuse the same name.
        null -> break@loop        // New note. Can still use this name.
        else -> continue@loop     // Conflict! Try another name.
      }
    }

    return uniqueName
  }

  private fun Note.folder(): String? {
    return if (isArchived) "archived" else null
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
    for (file in allRegisterFiles().reversed()) {
      val record = Record.from(registerDirectory, file)
      if (record.noteIdString !in noteIds) {
        file.delete()
      }
    }
  }
}

private fun File?.existsOrNull(): File? {
  return if (this?.exists == true) this else null
}

private inline fun <T> File?.hideAndRun(crossinline run: () -> T): T {
  return if (this == null) run()
  else {
    val origPath = this.path
    val renamedFile = renameTo(File(parent!!, "__temp"))
    val value = run()
    renamedFile.renameTo(File(origPath))
    value
  }
}

/**
 * @param relativePathWithoutExt Extension-less path of the register
 * file relative to directory where register files are stored.
 * E.g., "archived/uncharted___<uuid>".
 */
internal data class Record @Deprecated("Use Record.forFile()") constructor(
  private val relativePathWithoutExt: String
) {
  companion object {
    private const val SEPARATOR = "___"

    @Suppress("DEPRECATION")
    fun from(registersDirectory: File, registerFile: File): Record {
      check(registerFile.name.contains(SEPARATOR))
      return Record(registerFile.relativePathIn(registersDirectory))
    }

    fun writeToFile(registerDirectory: File, notesDirectory: File, noteFile: File, id: NoteId): File {
      val relativeName = noteFile.relativePathIn(notesDirectory).dropLast(".md".length)
      return File(registerDirectory, "$relativeName$SEPARATOR${id.value}").also {
        it.touch()
      }
    }
  }

  internal val noteFileName: String
    get() = relativePathWithoutExt.substringBefore(SEPARATOR).substringAfter("/")

  internal val noteId: NoteId
    get() = NoteId.from(noteIdString)

  internal val noteIdString: String
    get() = relativePathWithoutExt.substringAfter(SEPARATOR)

  internal val noteFolder: String
    get() = relativePathWithoutExt.substringBefore("/", missingDelimiterValue = "")

  internal fun registerFileIn(registersDirectory: File): File =
    File(registersDirectory, relativePathWithoutExt)

  internal fun noteFileIn(notesDirectory: File): File =
    File(File(notesDirectory, noteFolder), "$noteFileName.md")
}
