package me.saket.press.shared.db

import assertk.assertThat
import assertk.assertions.isEmpty
import me.saket.press.shared.containsOnly
import me.saket.press.shared.fakeFolder
import me.saket.press.shared.fakeNote
import me.saket.press.shared.testInsert
import kotlin.test.Test

class FolderQueriesTest : BaseDatabaeTest() {
  private val noteQueries get() = database.noteQueries
  private val folderQueries get() = database.folderQueries

  @Test fun `nonEmptyFoldersUnder() only includes folders that have non-empty notes`() {
    val folder1 = fakeFolder("folder1")
    val folder2 = fakeFolder("folder2")
    val folder3 = fakeFolder("folder3")
    folderQueries.testInsert(folder1, folder2, folder3)

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

  @Test fun `nonEmptyFolders() includes intermediate folders with empty notes if the leaf node contains a non-empty note`() {
    // both "archive" and "games" do not have any non-empty notes of their own,
    // but they have sub-folders that have non-empty notes so the entire hierarchy
    // should be included.
    val archive = fakeFolder("archive")
    val games = fakeFolder("blog", parent = archive.id)
    val rpg = fakeFolder("rpg", parent = games.id)
    folderQueries.testInsert(archive, games, rpg)

    noteQueries.testInsert(
      fakeNote("# ", folderId = games.id),
      fakeNote("# Horizon Zero Dawn", folderId = rpg.id)
    )

    val nonEmptyFolders = folderQueries.nonEmptyFoldersUnder(parent = null).executeAsList().map { it.name }
    assertThat(nonEmptyFolders).containsOnly("archive")
  }

  @Test fun `nonEmptyFolders() ignores intermediate folders with empty notes if the leaf node doesnt contain non-empty notes`() {
    val archive = fakeFolder("archive")
    val games = fakeFolder("blog", parent = archive.id)
    val rpg = fakeFolder("rpg", parent = games.id)
    folderQueries.testInsert(archive, games, rpg)

    noteQueries.testInsert(
      fakeNote("# ", folderId = games.id),
      fakeNote("", folderId = rpg.id)
    )

    val nonEmptyFolders = folderQueries.nonEmptyFoldersUnder(parent = null).executeAsList().map { it.name }
    assertThat(nonEmptyFolders).isEmpty()
  }

  @Test fun `foo`() {
    val archive = fakeFolder("archive")
    folderQueries.testInsert(archive)

    val nonEmptyFolders = folderQueries.nonEmptyFoldersUnder(parent = null).executeAsList().map { it.name }
    assertThat(nonEmptyFolders).isEmpty()
  }
}
