package me.saket.press.shared.db

import assertk.assertThat
import me.saket.press.shared.containsOnly
import me.saket.press.shared.fakedata.fakeFolder
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.syncer.git.insert
import me.saket.press.shared.syncer.git.testInsert
import kotlin.test.Test

class FolderQueriesTest : BaseDatabaeTest() {
  private val noteQueries get() = database.noteQueries
  private val folderQueries get() = database.folderQueries

  @Test fun `non empty folders`() {
    val folder1 = fakeFolder("folder1")
    val folder2 = fakeFolder("folder2")
    val folder3 = fakeFolder("folder3")
    folderQueries.insert(folder1, folder2, folder3)

    noteQueries.testInsert(
      fakeNote("Note 1", folderId = folder1.id),
      fakeNote("Note 2", folderId = folder1.id),
      fakeNote("Note 3", folderId = folder2.id),
      fakeNote("Note 4", folderId = folder3.id, isPendingDeletion = true),
      fakeNote("", folderId = folder3.id),
      fakeNote("# ", folderId = folder3.id),
      fakeNote("Note 5"),
    )

    val nonEmptyFolders = folderQueries.nonEmptyFoldersUnder(parent = null).executeAsList().map { it.name }
    assertThat(nonEmptyFolders).containsOnly(
      "folder1",
      "folder2"
    )
  }
}
