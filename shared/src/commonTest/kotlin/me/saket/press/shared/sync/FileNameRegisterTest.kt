package me.saket.press.shared.sync

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import me.saket.press.shared.RobolectricTest
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.sync.git.File
import me.saket.press.shared.sync.git.FileNameRegister
import me.saket.press.shared.testDeviceInfo
import kotlin.test.Test

class FileNameRegisterTest : RobolectricTest() {

  private val directory = testDeviceInfo().appStorage
  private val register = FileNameRegister(directory)

  @Test fun `generates unique file names to avoid conflicts`() {
    with(register) {
      val note1 = fakeNote(noteId = NoteId.generate(), content = "# abc")
      assertThat(fileFor(directory, note1).name).isEqualTo("abc.md")
      assertThat(noteIdFor("abc.md")).isEqualTo(note1.uuid)

      // Same note, updated content.
      val note2 = note1.copy(content = "# abc def")
      assertThat(fileFor(directory, note2).name).isEqualTo("abc_def.md")
      assertThat(noteIdFor("abc_def.md")).isEqualTo(note2.uuid)

      // Different note, same content.
      val note3 = fakeNote(noteId = NoteId.generate(), content = note1.content)
      assertThat(fileFor(directory, note3).name).isEqualTo("abc_2.md")
      assertThat(noteIdFor("abc_2.md")).isEqualTo(note3.uuid)
    }
  }

  @Test fun `generation of a new file name for resolving conflict`() {
    File(directory, "uncharted.md").write("A Thief's End")
    File(directory, "uncharted_2.md").write("The Lost Legacy")

    val conflictingFile = File(directory, "uncharted.md")
    val newName = register.findNewNameOnConflict(conflictingFile)
    assertThat(newName).isEqualTo("uncharted_3.md")
  }

  @Test fun `prune stale records`() {
    val note1 = fakeNote(content = "# Uncharted\nA Thief's End")
    val note2 = fakeNote(content = "# Uncharted\nThe Lost Legacy")
    val note1File = register.fileFor(directory, note1)
    val note2File = register.fileFor(directory, note2)

    assertThat(register.noteIdFor(note1File.name)).isEqualTo(note1.uuid)
    assertThat(register.noteIdFor(note2File.name)).isEqualTo(note2.uuid)

    register.pruneStaleRecords(latestNotes = listOf(note1))

    assertThat(register.noteIdFor(note1File.name)).isEqualTo(note1.uuid)
    assertThat(register.noteIdFor(note2File.name)).isNull()
  }
}
