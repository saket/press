package me.saket.press.shared.sync.git

import co.touchlab.stately.ensureNeverFrozen
import com.benasher44.uuid.uuidFrom
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.home.SplitHeadingAndBody
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.sync.git.FileNameSanitizer.sanitize
import kotlin.native.concurrent.ThreadLocal

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
@ThreadLocal
@OptIn(ExperimentalStdlibApi::class)
class FileNameRegister {
  init { ensureNeverFrozen() }

  fun read(fileDirectory: File, deviceId: Setting<DeviceId>): Reader {
    val registerDirectory = File(fileDirectory, ".press/registers")
    registerDirectory.makeDirectory(recursively = true)

    val deviceMappingsFile = File(registerDirectory, "register_${deviceId.get().id}")
    val otherMappingFiles = registerDirectory.children()
        .filter { it.name.startsWith("register_") }
        .filter { it.name != deviceMappingsFile.name }

    // todo: use kotlinx.serialization.
    val deserialize: (List<File>) -> Map<FileName, NoteId> = { files ->
      files.map { it.read() }
          .flatMap { it.split("\n") /* each line contains one record */ }
          .associate {
            val (name, id) = it.split("==")
            name to NoteId(uuidFrom(id))
          }
    }

    val deviceMappings = if (deviceMappingsFile.exists) {
      deserialize(listOf(deviceMappingsFile))
    } else {
      emptyMap()
    }
    return Reader(
        deviceMappings = deviceMappings.toMutableMap(),
        otherMappings = deserialize(otherMappingFiles),
        onSave = { save(fileDirectory, deviceMappingsFile, deviceMappings) }
    )
  }

  // todo: test
  private fun save(fileDirectory: File, deviceMappingsFile: File, mappings: Map<FileName, NoteId>) {
    // Prune stale mappings.
    val currentFiles = fileDirectory.children().map { it.name }
    val uptoDateMappings = mappings.filterKeys { it !in currentFiles }

    val serialized = uptoDateMappings.toList().joinToString(separator = "\n") { (name, id) -> "$name==${id.value}" }
    deviceMappingsFile.write(serialized)
  }

  class Reader internal constructor(
    val deviceMappings: MutableMap<FileName, NoteId>,
    private val otherMappings: Map<FileName, NoteId>,
    private val onSave: () -> Unit
  ) {

    /**
     * If a mapping does not exist, this file is either new or this register
     * was recreated after getting deleted. In both cases, a new ID should be
     * created.
     */
    fun noteIdFor(fileName: String): NoteId? {
      return deviceMappings[fileName] ?: otherMappings[fileName]
    }

    fun fileNameFor(note: Note): FileName {
      val (heading) = SplitHeadingAndBody.split(note.content)
      val expectedName = if (heading.isNotBlank()) heading else "untitled_note"

      // Suffix the name to avoid conflicts. E.g., "untitled_note_2".
      var uniqueName: String
      var conflicts = 0
      loop@ while (true) {
        uniqueName = sanitize(expectedName, maxLength = 240) + if (conflicts++ == 0) "" else "_$conflicts"

        when (noteIdFor("$uniqueName.md")) {
          note.uuid -> break@loop   // Reuse the same name.
          null -> break@loop        // New note. Can still use this name.
          else -> continue@loop     // Conflict! Try another name.
        }
      }

      return "$uniqueName.md".also {
        deviceMappings += it to note.uuid
      }
    }

    operator fun get(note: Note): FileName {
      return fileNameFor(note)
    }

    fun save() {
      onSave()
    }
  }
}
