package me.saket.press.shared.sync.git

import co.touchlab.stately.ensureNeverFrozen
import com.benasher44.uuid.uuidFrom
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.home.SplitHeadingAndBody
import kotlin.native.concurrent.ThreadLocal

typealias FileName = String

/**
 * Press tries really hard to avoid leaking Press's implementation leaking into user's git
 * repository. This includes using human readable filenames instead of UUIDs, generated note
 * titles. An unfortunate drawback is that filenames can collide despite having unique UUIDs
 * because users can create hundreds of notes titled "shopping checklist".
 *
 * This class maintains a mapping of filenames to their UUIDs. At the time of writing this,
 * I'm really hoping this works out alright. Otherwise, Press will have to start suffixing
 * filenames with 32 character long UUIDs 🤮.
 */
@ThreadLocal
class FileNameRegister(private val directory: File, private val deviceId: DeviceId) {

  private val mappings = mutableMapOf<FileName, NoteId>()

  init {
    ensureNeverFrozen()
  }

  fun use(block: (FileNameRegister) -> Unit) {
    readRegisters(directory)
    block(this)
    saveRegister(directory, deviceId)
  }

  private fun readRegisters(directory: File) {
    val registerFiles = directory
        .children()
        .filter { it.name.startsWith("register_") }

    mappings.putAll(registerFiles
        .map { it.read() }
        .flatMap { it.split("\n") /* each line contains one record */ }
        .associate {
          val (name, id) = it.split("==")
          name to NoteId(uuidFrom(id))
        })
  }

  // todo: test
  private fun saveRegister(directory: File, deviceId: DeviceId) {
    // Prune stale mappings.
    val fileNames = directory.children().map { it.name }
    mappings
        .filterKeys { it !in fileNames }
        .forEach { mappings.remove(it.key) }

    // todo: only save mappings that were created on this device
    val serializedMappings = mappings.toList().joinToString(separator = "\n") { (name, id) -> "$name==$id" }
    File(directory, "register_$deviceId").write(serializedMappings)
  }

  /**
   * If a mapping does not exist, this file is either new or this register
   * was recreated after getting deleted. In both cases, a new ID should be
   * created.
   */
  fun noteIdFor(fileName: String): NoteId? {
    return mappings[fileName]
  }

  fun fileNameFor(note: Note): FileName {
    val (heading) = SplitHeadingAndBody.split(note.content)
    val expectedName = if (heading.isNotBlank()) heading else "untitled_note"

    // Suffix the name to avoid collisions. E.g., "untitled_note_2".
    var uniqueName: String
    var conflictCount = 0
    loop@ while (true) {
      uniqueName = FileNameSanitizer.sanitize(expectedName, maxLength = 250)
          .plus(if (conflictCount++ == 0) "" else "_$conflictCount")

      when (mappings["$uniqueName.md"]) {
        note.uuid -> break@loop   // Reuse the same name.
        null -> break@loop        // New note. Can still use this name.
        else -> continue@loop     // Conflict! Try another name.
      }
    }

    return "$uniqueName.md".also {
      mappings += it to note.uuid
    }
  }
}
