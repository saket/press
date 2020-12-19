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

  private val archive = fakeFolder("archive")
  private val note1 = fakeNote("# Uncharted 1")
  private val note2 = fakeNote("# Uncharted 2", isPendingDeletion = true)
  private val note3 = fakeNote("# Uncharted 3", folderId = archive.id, isPendingDeletion = true)
  private val note4 = fakeNote("# Uncharted 4", folderId = archive.id)

  @Test fun `visible notes`() {
    folderQueries.insert(archive)
    noteQueries.testInsert(note1, note2, note3, note4)

    val visibleNotes = noteQueries.visibleNotes().executeAsList().map { it.content }
    assertThat(visibleNotes).containsOnly(note1.content)
  }

  @Test fun `archived notes`() {
    folderQueries.insert(archive)
    noteQueries.testInsert(note1, note2, note3, note4)

    val archivedNotes = noteQueries.archivedNotes().executeAsList().map { it.content }
    assertThat(archivedNotes).containsOnly(note3.content, note4.content)
  }
}
