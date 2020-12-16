package me.saket.press.shared.sync.git

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import me.saket.press.data.shared.Folder
import me.saket.press.shared.containsOnly
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.fakedata.fakeFolder
import kotlin.test.Test

class FolderPathsTest : BaseDatabaeTest() {
  private val queries get() = database.folderQueries
  private val paths = FolderPaths(database)

  @Test fun `create path`() {
    val grandParent = fakeFolder(name = "folder1")
    val parent = fakeFolder(name = "folder2", parent = grandParent.id)
    val child = fakeFolder(name = "folder3", parent = parent.id)
    queries.insert(grandParent, parent, child)

    assertThat(paths.createPath(grandParent.id)).isEqualTo("folder1")
    assertThat(paths.createPath(parent.id)).isEqualTo("folder1/folder2")
    assertThat(paths.createPath(child.id)).isEqualTo("folder1/folder2/folder3")
  }

  @Test fun `ensure folder paths`() {
    val savedFolders = { queries.allFolders().executeAsList() }
    fun List<Folder>.names() = map { it.name }
    fun List<Folder>.paths() = map { paths.createPath(it.id, existingFolders = this) }

    assertThat(paths.mkdirs("")).isNull()
    assertThat(queries.allFolders().executeAsList()).isEmpty()

    val folder1Id = paths.mkdirs("folder1")
    assertThat(folder1Id).isNotNull()
    assertThat(savedFolders().names()).containsOnly("folder1")

    val folder3Id = paths.mkdirs("folder1/folder2/folder1/folder3")!!
    assertThat(folder1Id).isNotNull()
    assertThat(paths.createPath(folder3Id)).isEqualTo("folder1/folder2/folder1/folder3")
    assertThat(savedFolders().names()).containsOnly(
      "folder1",
      "folder2",
      "folder1",
      "folder3"
    )
    assertThat(savedFolders().paths()).containsOnly(
      "folder1",
      "folder1/folder2",
      "folder1/folder2/folder1",
      "folder1/folder2/folder1/folder3"
    )
  }
}
