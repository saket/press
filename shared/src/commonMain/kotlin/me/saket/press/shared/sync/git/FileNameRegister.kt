package me.saket.press.shared.sync.git

import me.saket.press.PressDatabase
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.note.HeadingAndBody
import me.saket.press.shared.sync.git.FileNameSanitizer.sanitize

typealias FileName = String

/**
 * Press tries really hard to avoid leaking Press's implementation into user's git repository.
 * This includes using human readable filenames for saving notes, generated from notes' H1 headings.
 * An unfortunate drawback is that notes with the same title (e.g., "shopping checklist")
 * can't have the same filename. This class maintains a mapping of filenames to their IDs
 * to solve this.
 */
@OptIn(ExperimentalStdlibApi::class)
internal class FileNameRegister(
  private val notesDirectory: File,
  private val database: PressDatabase
) {
  private val folderPaths = FolderPaths(database)

  companion object {
    // Both NTFS and Unix file systems have a max-length of 255 letters.
    // Reserving 15 letters for handling conflicts and file name extension.
    private const val MAX_NAME_LENGTH = 240
  }

  private val registerDirectory = File(notesDirectory, ".press/registers").also {
    it.makeDirectories()
  }

  /**
   * @param noteRelativePath name relative to the notes directory.
   *
   * If a mapping does not exist, this file is either new or this register
   * was recreated after getting deleted. In both cases, a new ID should be
   * created.
   */
  @Suppress("NAME_SHADOWING")
  fun recordFor(noteRelativePath: String): Record? {
    return Record.from(registerDirectory, noteRelativePath)
  }

  fun recordFor(relativePath: FileName, oldPath: String?): Record? {
    val oldRecord = oldPath?.let { recordFor(it) }
      ?: return recordFor(relativePath)

    val noteFile = File(notesDirectory, relativePath)
    return createNewRecordFor(noteFile, id = oldRecord.noteId).also {
      oldRecord.delete()
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
    private val notesDirectory: File,
    val suggestedFile: File,
    val oldFile: File? = null,
    val acceptRename: (() -> Unit)? = null
  ) {
    val suggestedFilePath get() = suggestedFile.relativePathIn(notesDirectory)
    val oldFilePath get() = oldFile?.relativePathIn(notesDirectory)
  }

  @Suppress("CascadeIf")
  fun suggestFile(note: Note): FileSuggestion {
    val oldRecord = recordFor(note)
    val oldNoteFile = oldRecord?.noteFileIn(notesDirectory).existsOrNull()

    val newNoteName = oldNoteFile.hideAndRun {
      // The old file (if any) needs to be hidden or else it'll
      // be seen as a conflict when a new name is generated.
      generateNameFor(note, canUseExisting = true)
    }

    return if (oldNoteFile == null) {
      // New note. A new file needs to be created.
      FileSuggestion(
        notesDirectory,
        File(notesDirectory, newNoteName).also {
          it.parent?.makeDirectories()
          createNewRecordFor(it, note.id)
        }
      )
    } else if (oldNoteFile.relativePathIn(notesDirectory) == newNoteName) { // todo: use oldRecord.noteFilePath?
      // A file already exists and the name matches the note's heading.
      FileSuggestion(notesDirectory, oldNoteFile)
    } else {
      // A file already exists, but the heading/folder was changed. Rename the file.
      val newFile = File(notesDirectory, newNoteName)
      FileSuggestion(
        notesDirectory, newFile, oldNoteFile,
        acceptRename = {
          oldNoteFile.renameTo(newFile)
          oldRecord!!.delete()
          createNewRecordFor(newFile, note.id)
        }
      )
    }
  }

  fun createNewRecordFor(noteFile: File, id: NoteId): Record {
    require(noteFile.extension == "md")

    if (!registerDirectory.exists) {
      registerDirectory.makeDirectories()
    }

    val recordFile = Record.writeToFile(registerDirectory, notesDirectory, noteFile, id)
    return Record.from(registerDirectory, recordFile)
  }

  private fun allRegisterFiles(folder: String = ""): List<File> =
    with(File(registerDirectory, folder)) {
      return when {
        !exists -> emptyList()
        else -> children(recursively = true)
      }
    }

  /** Finds a file name for `note` if it already exists or generates a new one. */
  fun generateNameFor(note: Note, canUseExisting: Boolean): FileName {
    val (heading) = HeadingAndBody.parse(note.content)
    val expectedName = if (heading.isNotBlank()) heading else "untitled_note"
    val folder = note.folder()?.plus("/") ?: ""

    val existingNames = notesDirectory.children(recursively = true)
      .map { it.relativePathIn(notesDirectory) }
      .filter { it.endsWith(".md") }

    // Suffix the name to avoid conflicts, e.g., "untitled_note_2".
    var uniqueName: String
    var conflicts = 0
    while (true) {
      val conflictSuffix = if (conflicts++ == 0) "" else "_$conflicts"
      uniqueName = "$folder${sanitize(expectedName, MAX_NAME_LENGTH)}$conflictSuffix.md"

      if (canUseExisting && noteIdFor(uniqueName) == note.id) {
        // A file already exists for this note. Reuse the same name.
        break

      } else if (uniqueName !in existingNames) {
        break
      }

      // Else, there may be files with the same name but with different IDs
      // (or no IDs if they were just synced and haven't been processed yet).
    }

    return uniqueName
  }

  private fun Note.folder(): String? {
    val folderPath = folderId?.let(folderPaths::createPath)
    return when {
      isArchived -> "archived" + (folderPath?.prefix("/") ?: "")
      else -> folderPath
    }
  }

  fun pruneStaleRecords(currentIds: Collection<NoteId>) {
    if (!registerDirectory.exists) return

    val noteIds = currentIds.map { it.value.toString() }
    for (file in allRegisterFiles().reversed()) {
      val record = Record.from(registerDirectory, file)
      if (record.noteIdString !in noteIds) {
        file.delete()
      }
    }
  }

  fun pruneDuplicateRecords() {
    if (!registerDirectory.exists) return

    val records = allRegisterFiles().map { Record.from(registerDirectory, it) }
    val uniqueIds = records.associateBy { it.noteIdString }

    for (record in records.reversed()) {
      val unique = uniqueIds[record.noteIdString]!!
      if (record.noteFilePath != unique.noteFilePath) {
        record.delete()
      }
    }
  }

  internal class Record private constructor(
    private val registersDirectory: File,
    internal val registerFile: File
  ) {
    companion object {
      fun from(registerDirectory: File, relativePath: String): Record? {
        require(relativePath.endsWith("md")) { "Not a note: $relativePath" }

        val registerFile = File(registerDirectory, relativePath.dropLast(".md".length))
          .existsOrNull() ?: return null
        return from(registerDirectory, registerFile)
      }

      fun from(registersDirectory: File, registerFile: File): Record {
        return Record(registersDirectory, registerFile)
      }

      fun writeToFile(registerDirectory: File, notesDirectory: File, noteFile: File, id: NoteId): File {
        val relativePath = noteFile.relativePathIn(notesDirectory)
        check(relativePath.endsWith(".md"))

        return File(registerDirectory, relativePath.dropLast(".md".length)).also {
          if (!it.parent.exists) it.parent.makeDirectories()
          it.write(id.value.toString())
        }
      }
    }

    internal val noteFilePath: String
      get() = "${registerFile.relativePathIn(registersDirectory)}.md"

    internal val noteId: NoteId
      get() = NoteId.from(noteIdString)

    internal val noteIdString: String
      get() = registerFile.read()

    internal val noteFolder: String
      get() = noteFilePath.substringBeforeLast("/", missingDelimiterValue = "")

    internal fun noteFileIn(notesDirectory: File): File {
      return File(notesDirectory, noteFilePath)
    }

    internal fun delete() {
      registerFile.delete()
    }
  }
}

private fun String.prefix(prefix: String): String {
  return prefix + this
}

private inline fun <T> File?.hideAndRun(crossinline run: () -> T): T {
  return if (this == null) run()
  else {
    val origPath = this.path
    val renamedFile = renameTo(File(parent, "__temp"))
    val value = run()
    renamedFile.renameTo(File(origPath))
    value
  }
}
