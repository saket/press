package me.saket.press.shared.sync

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.sync.git.File
import me.saket.press.shared.sync.git.FileNameRegister
import me.saket.press.shared.sync.git.touch
import me.saket.press.shared.testDeviceInfo
import kotlin.test.Test

class FileNameRegisterTest {

  private val directory = testDeviceInfo().appStorage
  private val register = FileNameRegister(directory)

  @Test fun canary() {
    val archivedDir = File(directory, "archived").apply { makeDirectory() }
    val noteFile = File(archivedDir, "uncharted.md").apply { write("A Thief's End") }
    val noteId = NoteId.generate()

    val record = register.createNewRecordFor(noteFile, noteId)

    assertThat(record.noteFilePath).isEqualTo("archived/uncharted.md")
    assertThat(record.noteFolder).isEqualTo("archived")
    assertThat(record.noteId).isEqualTo(noteId)
    assertThat(record.noteFileIn(directory).path).isEqualTo(noteFile.path)
    assertThat(record.registerFile.relativePathIn(register.registerDirectory)).isEqualTo("archived/${noteId.value}")
  }

  @Test fun `generates unique file names to avoid conflicts`() {
    with(register) {
      val note = fakeNote(id = NoteId.generate(), content = "# abc")
      assertThat(fileFor(note).name).isEqualTo("abc.md")
      assertThat(noteIdFor("abc.md")).isEqualTo(note.id)

      // Same note ID, different content.
      val updatedNote1 = note.copy(content = "# abc def")
      assertThat(fileFor(updatedNote1).name).isEqualTo("abc_def.md")
      assertThat(noteIdFor("abc_def.md")).isEqualTo(updatedNote1.id)

      // Different note ID, same content.
      val note3 = fakeNote(id = NoteId.generate(), content = updatedNote1.content)
      assertThat(fileFor(note3).name).isEqualTo("abc_def_2.md")
      assertThat(noteIdFor("abc_def_2.md")).isEqualTo(note3.id)
    }
  }

  @Test fun `rename file if note's heading changes`() {
    val note = fakeNote(id = NoteId.generate(), content = "# abc")

    val fileBeforeUpdate = register.fileFor(note).touch()
    assertThat(fileBeforeUpdate.name).isEqualTo("abc.md")
    assertThat(fileBeforeUpdate.exists).isTrue()
    assertThat(register.noteIdFor("abc.md")).isEqualTo(note.id)

    val fileAfterUpdate = register.fileFor(note.copy(content = "# abcdef"))
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
    val note1File = register.fileFor(note1)
    val note2File = register.fileFor(note2)

    assertThat(register.noteIdFor(note1File.name)).isEqualTo(note1.id)
    assertThat(register.noteIdFor(note2File.name)).isEqualTo(note2.id)

    register.pruneStaleRecords(latestNotes = listOf(note1))

    assertThat(register.noteIdFor(note1File.name)).isEqualTo(note1.id)
    assertThat(register.noteIdFor(note2File.name)).isNull()
  }

  @Test fun `support for archived folder`() {
    val note = fakeNote(content = "# The Witcher 3\nWild hunt", isArchived = false)

    val unarchivedFile = register.fileFor(note).touch()
    assertThat(unarchivedFile.exists).isTrue()
    assertThat(unarchivedFile.relativePathIn(directory)).isEqualTo("the_witcher_3.md")
    with(register.recordFor("the_witcher_3.md")!!) {
      assertThat(noteId).isEqualTo(note.id)
      assertThat(noteFolder).isEmpty()
    }

    val archivedFile = register.fileFor(note.copy(isArchived = true))
    assertThat(archivedFile.relativePathIn(directory)).isEqualTo("archived/the_witcher_3.md")
    assertThat(unarchivedFile.exists).isFalse()
    with(register.recordFor("archived/the_witcher_3.md")!!) {
      assertThat(noteId).isEqualTo(note.id)
      assertThat(noteFolder).isEqualTo("archived")
    }

    register.fileFor(note.copy(isArchived = false)).touch()
    assertThat(archivedFile.exists).isFalse()
    assertThat(unarchivedFile.exists).isTrue()
    with(register.recordFor("the_witcher_3.md")!!) {
      assertThat(noteId).isEqualTo(note.id)
      assertThat(noteFolder).isEmpty()
    }
  }

  private fun FileNameRegister.fileFor(note: Note): File {
    val suggestion = suggestFile(note).apply { acceptRename() }
    return suggestion.suggestedFile
  }
}
