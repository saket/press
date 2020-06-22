package me.saket.press.shared.sync

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
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
      val note = fakeNote(noteId = NoteId.generate(), content = "# abc")
      assertThat(fileFor(directory, note).name).isEqualTo("abc.md")
      assertThat(noteIdFor("abc.md")).isEqualTo(note.id)

      // Same note, updated content.
      val updatedNote1 = note.copy(content = "# abc def")
      assertThat(fileFor(directory, updatedNote1).name).isEqualTo("abc_def.md")
      assertThat(noteIdFor("abc_def.md")).isEqualTo(updatedNote1.id)

      // Different note, same content.
      val note3 = fakeNote(noteId = NoteId.generate(), content = note.content)
      assertThat(fileFor(directory, note3).name).isEqualTo("abc_2.md")
      assertThat(noteIdFor("abc_2.md")).isEqualTo(note3.id)
    }
  }

  @Test fun `rename file if note's heading changes`() {
    val note = fakeNote(noteId = NoteId.generate(), content = "# abc")

    val fileBeforeUpdate = register.fileFor(directory, note)
    fileBeforeUpdate.write(note.content)

    assertThat(fileBeforeUpdate.name).isEqualTo("abc.md")
    assertThat(fileBeforeUpdate.exists).isTrue()
    assertThat(register.noteIdFor("abc.md")).isEqualTo(note.id)

    val fileAfterUpdate = register.fileFor(directory, note.copy(content = "# abcdef"))
    assertThat(fileAfterUpdate.name).isEqualTo("abcdef.md")
    assertThat(fileBeforeUpdate.exists).isFalse()

    assertThat(register.noteIdFor("abc.md")).isNull()
    assertThat(register.noteIdFor("abcdef.md")).isEqualTo(note.id)
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

    assertThat(register.noteIdFor(note1File.name)).isEqualTo(note1.id)
    assertThat(register.noteIdFor(note2File.name)).isEqualTo(note2.id)

    register.pruneStaleRecords(latestNotes = listOf(note1))

    assertThat(register.noteIdFor(note1File.name)).isEqualTo(note1.id)
    assertThat(register.noteIdFor(note2File.name)).isNull()
  }
}
