package me.saket.press.shared.sync.git

import co.touchlab.stately.ensureNeverFrozen
import com.benasher44.uuid.uuidFrom
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.home.SplitHeadingAndBody
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
class FileNameRegister(private val directory: File, private val deviceId: DeviceId) {
  private val deviceMappingFileName get() = "register_${deviceId.id}"

  init {
    ensureNeverFrozen()
  }

  fun read(): Reader {
    return readRegisters(directory)
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun readRegisters(directory: File): Reader {
    directory.makeDirectory(recursively = true)

    val (deviceMappingsFile, otherMappingsFile) = directory
        .children()
        .filter { it.name.startsWith("register_") }
        .partition { it.name == deviceMappingFileName }

    // todo: use kotlinx.serialization.
    val deserialize: (List<File>) -> Map<FileName, NoteId> = { files ->
      files.map { it.read() }
          .flatMap { it.split("\n") /* each line contains one record */ }
          .associate {
            val (name, id) = it.split("==")
            name to NoteId(uuidFrom(id))
          }
    }

    val deviceMappings = deserialize(deviceMappingsFile).toMutableMap()
    return Reader(
        deviceMappings = deviceMappings,
        otherMappings = deserialize(otherMappingsFile),
        onSave = { save(deviceMappings) }
    )
  }

  // todo: test
  private fun save(mappings: Map<FileName, NoteId>) {
    // Prune stale mappings.
    val currentFiles = directory.children().map { it.name }
    val uptoDateMappings = mappings.filterKeys { it !in currentFiles }

    val serialized = uptoDateMappings.toList().joinToString(separator = "\n") { (name, id) -> "$name==${id.value}" }
    File(directory, deviceMappingFileName).write(serialized)
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
