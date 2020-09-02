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

  internal val registerDirectory = File(notesDirectory, ".press/registers").also {
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

    // Example: "archived/uncharted.md"
    val folderName = relativePath.substringBefore("/", missingDelimiterValue = "")  // e.g., "archived"

    for (file in allRegisterFiles(folderName)) {
      val record = Record.from(registerDirectory, file)
      if (record.noteFilePath == relativePath) {
        return record
      }
    }
    return null
  }

  fun recordFor(relativePath: FileName, oldPath: String?): Record? {
    val oldRecord = oldPath?.let { recordFor(it) }

    return if (oldRecord == null) {
      recordFor(relativePath)
    } else {
      oldRecord.registerFile.delete()
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

  data class FileSuggestion(
    val suggestedFile: File,
    val oldFile: File? = null,
    val acceptRename: (() -> Unit)? = null
  )

  @Suppress("CascadeIf")
  fun suggestFile(note: Note): FileSuggestion {
    val oldRecord = recordFor(note)
    val oldNoteFile = oldRecord?.noteFileIn(notesDirectory).existsOrNull()

    val newNoteName = oldNoteFile.hideAndRun {
      // The old file (if any) needs to be hidden or else it'll
      // be seen as a conflict when a new name is generated.
      findOrGenerateFileNameFor(note)
    }

    return if (oldNoteFile == null) {
      // New note. A new file needs to be created.
      FileSuggestion(File(notesDirectory, newNoteName).also {
        it.parent?.makeDirectory(recursively = true)
        createNewRecordFor(it, note.id)
      })
    } else if (oldNoteFile.relativePathIn(notesDirectory) == newNoteName) {
      // A file already exists and the name matches the note's heading.
      FileSuggestion(oldNoteFile)
    } else {
      // A file already exists, but the heading/folder was changed. Rename the file.
      val newFile = File(notesDirectory, newNoteName)
      FileSuggestion(newFile, oldFile = oldNoteFile, acceptRename = {
        oldNoteFile.renameTo(newFile)
        oldRecord!!.registerFile.delete()
        createNewRecordFor(newFile, note.id)
      })
    }
  }

  fun createNewRecordFor(noteFile: File, id: NoteId): Record {
    require(noteFile.extension == "md")

    if (!registerDirectory.exists) {
      registerDirectory.makeDirectory(recursively = true)
    }

    val recordFile = Record.writeToFile(registerDirectory, notesDirectory, noteFile, id)
    return Record.from(registerDirectory, recordFile)
  }

  private fun allRegisterFiles(folder: String = ""): List<File> =
    with(File(registerDirectory, folder)) {
      return when {
        !exists -> emptyList()
        else -> children(recursively = true).filter { !it.isDirectory }
      }
    }

  /** Finds a file name for `note` if it already exists or generates a new one. */
  private fun findOrGenerateFileNameFor(note: Note): String {
    val (heading) = SplitHeadingAndBody.split(note.content)
    val expectedName = if (heading.isNotBlank()) heading else "untitled_note"
    val folder = note.folder()?.plus("/") ?: ""

    val existingNames = File(notesDirectory, folder)
        .existsOrNull()
        ?.children()?.map { it.name }
        ?: emptyList()

    // Suffix the name to avoid conflicts. E.g., "untitled_note_2".
    var uniqueName: String
    var conflicts = 0
    while (true) {
      val conflictSuffix = if (conflicts++ == 0) "" else "_$conflicts"
      uniqueName = "$folder${sanitize(expectedName, MAX_NAME_LENGTH)}$conflictSuffix.md"

      if (noteIdFor(uniqueName) == note.id) {
        // A file already exists for this note. Reuse the same name.
        break

      } else if (uniqueName !in existingNames) {
        // There may be files with the same name but with different IDs
        // (or no IDs if they were just synced and haven't been processed yet).
        break
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

internal data class Record @Deprecated("Use Record.forFile()") constructor(
  private val registersDirectory: File,
  val registerFile: File
) {
  companion object {
    @Suppress("DEPRECATION")
    fun from(registersDirectory: File, registerFile: File): Record {
      return Record(registersDirectory, registerFile)
    }

    fun writeToFile(registerDirectory: File, notesDirectory: File, noteFile: File, id: NoteId): File {
      val relativePath = noteFile.relativePathIn(notesDirectory)
      val relativeFolder = noteFile.parent?.relativePathIn(notesDirectory) ?: ""

      return File(File(registerDirectory, relativeFolder), "${id.value}").also {
        it.parent?.let { p -> if (!p.exists) p.makeDirectory(recursively = true) }
        it.write(relativePath)
      }
    }
  }

  internal val noteFilePath: String
    get() = registerFile.read()

  internal val noteId: NoteId
    get() = NoteId.from(noteIdString)

  internal val noteIdString: String
    get() = registerFile.name

  internal val noteFolder: String
    get() {
      val relativePath = registerFile.relativePathIn(registersDirectory)
      return relativePath.substringBefore("/", missingDelimiterValue = "")
    }

  internal fun noteFileIn(notesDirectory: File): File {
    return File(notesDirectory, noteFilePath)
  }
}
