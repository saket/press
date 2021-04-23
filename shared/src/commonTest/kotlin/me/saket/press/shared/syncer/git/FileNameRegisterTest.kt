package me.saket.press.shared.syncer.git

import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import me.saket.press.data.shared.Note
import me.saket.press.shared.containsOnly
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.fakeFolder
import me.saket.press.shared.fakeNote
import me.saket.press.shared.testDeviceInfo
import kotlin.test.AfterTest
import kotlin.test.Test

class FileNameRegisterTest : BaseDatabaeTest() {
  private val directory = testDeviceInfo().appStorage
  private val register = FileNameRegister(
    notesDirectory = directory,
    database = database
  )

  @AfterTest
  fun cleanup() {
    directory.delete(recursively = true)
  }

  @Test fun canary() {
    val archivedDir = File(directory, "archive/games").apply { makeDirectories() }
    val noteFile = File(archivedDir, "uncharted.md").apply { write("A Thief's End") }
    val noteId = NoteId.generate()

    val record = register.createNewRecordFor(noteFile, noteId)
    assertThat(record.noteFilePath).isEqualTo("archive/games/uncharted.md")
    assertThat(record.noteFolder).isEqualTo("archive/games")
    assertThat(record.noteId).isEqualTo(noteId)

    assertThat(record.noteFileIn(directory).relativePathIn(directory)).isEqualTo("archive/games/uncharted.md")
    assertThat(record.registerFile.path).endsWith(".press/registers/archive/games/uncharted.meta")
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

  @Test fun `generation of a new file name for resolving conflict in the same folder`() {
    File(directory, "uncharted.md").write("A Thief's End")
    File(directory, "uncharted_2.md").write("The Lost Legacy")
    File(directory, "archive/uncharted_3.md").touch().write("Archived The Lost Legacy")

    val conflictingNote = fakeNote("# Uncharted\nDrake's Fortune")
    val newName = register.generateNameFor(conflictingNote, canUseExisting = false)
    assertThat(newName).isEqualTo("uncharted_3.md")
  }

  @Test fun `prune stale records`() {
    val note1 = fakeNote(content = "# Uncharted\nA Thief's End")
    val note2 = fakeNote(content = "# Uncharted\nThe Lost Legacy")
    val note1File = register.fileFor(note1)
    val note2File = register.fileFor(note2)

    assertThat(register.noteIdFor(note1File.name)).isEqualTo(note1.id)
    assertThat(register.noteIdFor(note2File.name)).isEqualTo(note2.id)

    register.pruneStaleRecords(currentIds = listOf(note1.id))

    assertThat(register.noteIdFor(note1File.name)).isEqualTo(note1.id)
    assertThat(register.noteIdFor(note2File.name)).isNull()
  }

  @Test fun `migrate from invalid records that point to the same ID`() {
    val note1 = fakeNote("# ")
    val record1 = register.createNewRecordFor(register.fileFor(note1), note1.id)

    val note2 = fakeNote("# ")
    val record2 = register.createNewRecordFor(register.fileFor(note2), note2.id)
    record2.registerFile.write("${note1.id.value}")

    assertThat(register.noteIdFor(record1.noteFilePath)).isEqualTo(register.noteIdFor(record2.noteFilePath))
    register.migrateRecords()
    assertThat(register.noteIdFor(record1.noteFilePath)).isNotEqualTo(register.noteIdFor(record2.noteFilePath))
  }

  @Test fun `migrate from old record file names`() {
    val gamesFolder = fakeFolder("games", parent = null)
    val witcherNote = fakeNote("# witcher 3", folderId = gamesFolder.id)
    database.folderQueries.insert(gamesFolder)
    register.createNewRecordFor(register.fileFor(witcherNote), witcherNote.id)

    val finNote = fakeNote("# finances")
    val finRecord = register.createNewRecordFor(register.fileFor(finNote), finNote.id)
    val finRecordName = finRecord.registerFile.name

    // Old record names didn't have any extension.
    check(finRecordName.endsWith(".meta"))
    finRecord.registerFile.renameTo(finRecordName.replaceSuffix(".meta", with = ""))

    // Folder names don't have any extension either, so make sure they don't get migrated as well.
    File(finRecord.registerFile.parent, "games").makeDirectories()

    register.migrateRecords()

    val resultingPaths = register.registerDirectory
      .children(recursively = true)
      .map { it.relativePathIn(register.registerDirectory) }

    assertThat(resultingPaths).containsOnly(
      "games/witcher_3.meta",
      "finances.meta",
    )
  }

  @Test fun `detect updates to note's folder`() {
    val note = fakeNote(content = "# The Witcher 3\nWild hunt", folderId = null)

    val unarchivedFile = register.fileFor(note)
    assertThat(unarchivedFile.exists).isTrue()
    assertThat(unarchivedFile.relativePathIn(directory)).isEqualTo("the_witcher_3.md")
    with(register.recordFor("the_witcher_3.md")!!) {
      assertThat(noteId).isEqualTo(note.id)
      assertThat(noteFolder).isEmpty()
    }

    val archive = fakeFolder("archive")
    database.folderQueries.insert(archive)

    val archivedFile = register.fileFor(note.copy(folderId = archive.id))
    assertThat(archivedFile.relativePathIn(directory)).isEqualTo("archive/the_witcher_3.md")
    assertThat(unarchivedFile.exists).isFalse()
    with(register.recordFor("archive/the_witcher_3.md")!!) {
      assertThat(noteId).isEqualTo(note.id)
      assertThat(noteFolder).isEqualTo("archive")
    }

    register.fileFor(note.copy(folderId = null))
    assertThat(archivedFile.exists).isFalse()
    assertThat(unarchivedFile.exists).isTrue()
    with(register.recordFor("the_witcher_3.md")!!) {
      assertThat(noteId).isEqualTo(note.id)
      assertThat(noteFolder).isEmpty()
    }
  }

  @Test fun `support for single folder`() {
    val gamesFolder = fakeFolder("games", parent = null)
    database.folderQueries.insert(gamesFolder)

    val note = fakeNote(
      content = "# The Witcher 3\nHearts of Stone",
      folderId = gamesFolder.id
    )
    val file = register.fileFor(note)

    assertThat(file.relativePathIn(directory)).isEqualTo("games/the_witcher_3.md")
    with(register.recordFor("games/the_witcher_3.md")!!) {
      assertThat(noteId).isEqualTo(note.id)
      assertThat(noteFolder).isEqualTo("games")
    }
  }

  @Test fun `support for archived note inside a single folder`() {
    val archiveFolder = fakeFolder("archive", parent = null)
    val gamesFolder = fakeFolder("Games", parent = archiveFolder.id)
    database.folderQueries.insert(archiveFolder, gamesFolder)

    val note = fakeNote(
      content = "# The Witcher 3\nHearts of Stone",
      folderId = gamesFolder.id
    )
    val file = register.fileFor(note)

    assertThat(file.relativePathIn(directory)).isEqualTo("archive/Games/the_witcher_3.md")
    with(register.recordFor("archive/Games/the_witcher_3.md")!!) {
      assertThat(noteId).isEqualTo(note.id)
      assertThat(noteFolder).isEqualTo("archive/Games")
    }
  }

  @Test fun `support for nested folders`() {
    val gamesFolder = fakeFolder("Games", parent = null)
    val witcherFolder = fakeFolder("The Witcher 3", parent = gamesFolder.id)
    database.folderQueries.insert(gamesFolder, witcherFolder)

    val note = fakeNote(
      content = "# Hearts of Stone",
      folderId = witcherFolder.id
    )
    val file = register.fileFor(note)

    assertThat(file.relativePathIn(directory)).isEqualTo("Games/The Witcher 3/hearts_of_stone.md")
    with(register.recordFor("Games/The Witcher 3/hearts_of_stone.md")!!) {
      assertThat(noteId).isEqualTo(note.id)
      assertThat(noteFolder).isEqualTo("Games/The Witcher 3")
    }
  }

  @Test fun `support for archived note inside nested folders`() {
    val archiveFolder = fakeFolder("archive", parent = null)
    val gamesFolder = fakeFolder("Games", parent = archiveFolder.id)
    val witcherFolder = fakeFolder("The Witcher 3", parent = gamesFolder.id)
    database.folderQueries.insert(archiveFolder, gamesFolder, witcherFolder)

    val note = fakeNote(
      content = "# Hearts of Stone",
      folderId = witcherFolder.id
    )
    val file = register.fileFor(note)

    assertThat(file.relativePathIn(directory)).isEqualTo("archive/Games/The Witcher 3/hearts_of_stone.md")
    with(register.recordFor("archive/Games/The Witcher 3/hearts_of_stone.md")!!) {
      assertThat(noteId).isEqualTo(note.id)
      assertThat(noteFolder).isEqualTo("archive/Games/The Witcher 3")
    }
  }

  @Test fun `regression test - note with the same name as a folder shouldn't cause an error`() {
    val financesFolder = fakeFolder("finances")
    database.folderQueries.insert(financesFolder)

    val randomNote = fakeNote("# Random", folderId = financesFolder.id)
    register.fileFor(randomNote)

    val financesNote = fakeNote(
      content = "# Finances",
      folderId = null
    )
    register.fileFor(financesNote)
  }

  private fun FileNameRegister.fileFor(note: Note): File {
    val suggestion = suggestFile(note).apply { acceptRename?.invoke() }
    return suggestion.suggestedFile.also { it.touch() }
  }
}
