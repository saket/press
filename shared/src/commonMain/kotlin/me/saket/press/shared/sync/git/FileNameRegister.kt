package me.saket.press.shared.sync.git

import com.benasher44.uuid.uuidFrom
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.home.SplitHeadingAndBody
import me.saket.press.shared.sync.git.FileNameSanitizer.sanitize

private typealias FileName = String

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
   * If a mapping does not exist, this file is either new or this register
   * was recreated after getting deleted. In both cases, a new ID should be
   * created.
   */
  @Suppress("NAME_SHADOWING")
  fun noteIdFor(fileName: String): NoteId? {
    require(fileName.endsWith("md")) { "Not a note: $fileName" }
    val fileName = fileName.substringBeforeLast(".")

    if (!registerDirectory.exists) {
      // Likely checking out a remote commit that
      // doesn't have the registers directory yet.
      return null
    }

    for (file in registerDirectory.children()) {
      val (name, id) = deserialize(registerName = file.name)
      if (name == fileName) {
        return NoteId(uuidFrom(id))
      }
    }
    return null
  }

  fun fileNameFor(note: Note): FileName {
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

    return "$uniqueName.md".also {
      if (!registerDirectory.exists) {
        registerDirectory.makeDirectory(recursively = true)
      }
      val serializedName = serialize(noteFileName = uniqueName, id = note.uuid)
      File(registerDirectory, serializedName).write("")
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
}
