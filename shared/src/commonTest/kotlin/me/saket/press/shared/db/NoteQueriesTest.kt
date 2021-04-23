package me.saket.press.shared.db

import assertk.assertThat
import me.saket.press.shared.containsOnly
import me.saket.press.shared.fakeFolder
import me.saket.press.shared.fakeNote
import me.saket.press.shared.syncer.git.testInsert
import kotlin.test.Test

class NoteQueriesTest : BaseDatabaeTest() {
  private val noteQueries get() = database.noteQueries
  private val folderQueries get() = database.folderQueries

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
