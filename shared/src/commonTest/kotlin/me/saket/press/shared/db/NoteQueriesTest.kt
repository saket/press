package me.saket.press.shared.db

import assertk.assertThat
import me.saket.press.shared.containsOnly
import me.saket.press.shared.fakedata.fakeFolder
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.sync.git.insert
import me.saket.press.shared.sync.git.testInsert
import kotlin.test.Test

class NoteQueriesTest : BaseDatabaeTest() {
  private val noteQueries get() = database.noteQueries
  private val folderQueries get() = database.folderQueries

  @Test fun `visible non-empty notes`() {
    val folder1 = fakeFolder("folder1")
    val folder2 = fakeFolder("folder2")
    val folder3 = fakeFolder("folder3")
    folderQueries.insert(folder1, folder2, folder3)

    noteQueries.testInsert(
      fakeNote("Note 1"),
      fakeNote("Note 2", isPendingDeletion = true),
      fakeNote(""),
      fakeNote("   "),
      fakeNote("#  "),
    )

    val visibleNotes = noteQueries.visibleNonEmptyNotesInFolder(folderId = null).executeAsList().map { it.content }
    assertThat(visibleNotes).containsOnly("Note 1")
  }

  @Test fun `archived notes`() {
    val archive = fakeFolder("archive")
    val note1 = fakeNote("# Uncharted 1")
    val note2 = fakeNote("# Uncharted 2", isPendingDeletion = true)
    val note3 = fakeNote("# Uncharted 3", folderId = archive.id, isPendingDeletion = true)
    val note4 = fakeNote("# Uncharted 4", folderId = archive.id)

    folderQueries.insert(archive)
    noteQueries.testInsert(note1, note2, note3, note4)

    val archivedNotes = noteQueries.archivedNotes().executeAsList().map { it.content }
    assertThat(archivedNotes).containsOnly(note3.content, note4.content)
  }
}
