package me.saket.press.shared.syncer.git

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import me.saket.press.data.shared.Folder
import me.saket.press.shared.containsOnly
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.db.FolderId
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.fakeFolder
import me.saket.press.shared.fakeNote
import kotlin.test.Test

class FolderPathsTest : BaseDatabaeTest() {
  private val noteQueries get() = database.noteQueries
  private val folderQueries get() = database.folderQueries
  private val paths = FolderPaths(database)

  @Test fun `create path`() {
    val grandParent = fakeFolder(name = "folder1")
    val parent = fakeFolder(name = "folder2", parent = grandParent.id)
    val child = fakeFolder(name = "folder3", parent = parent.id)
    folderQueries.insert(grandParent, parent, child)

    assertThat(paths.createFlatPath(grandParent.id)).isEqualTo("folder1")
    assertThat(paths.createFlatPath(parent.id)).isEqualTo("folder1/folder2")
    assertThat(paths.createFlatPath(child.id)).isEqualTo("folder1/folder2/folder3")
  }

  @Test fun `ensure folder paths`() {
    val savedFolders = { folderQueries.allFolders().executeAsList() }
    fun List<Folder>.names() = map { it.name }
    fun List<Folder>.paths() = map { paths.createFlatPath(it.id, existingFolders = { this }) }

    assertThat(paths.mkdirs("")).isNull()
    assertThat(folderQueries.allFolders().executeAsList()).isEmpty()

    val folder1Id = paths.mkdirs("folder1")
    assertThat(folder1Id).isNotNull()
    assertThat(savedFolders().names()).containsOnly("folder1")

    val folder3Id = paths.mkdirs("folder1/folder2/folder1/folder3")!!
    assertThat(folder1Id).isNotNull()
    assertThat(paths.createFlatPath(folder3Id)).isEqualTo("folder1/folder2/folder1/folder3")
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

  @Test fun `archive note`() {
    val showsId = FolderId.generate()
    val noteId = NoteId.generate()
    folderQueries.insert(fakeFolder("shows", id = showsId))
    noteQueries.testInsert(fakeNote("# Mandalorian", id = noteId, folderId = showsId))

    val folderPath = {
      val savedNote = noteQueries.note(noteId).executeAsOne()
      paths.createFlatPath(savedNote.folderId)
    }
    assertThat(folderPath()).isEqualTo("shows")

    paths.setArchived(noteId, archive = true)
    assertThat(folderPath()).isEqualTo("archive/shows")

    assertThat {
      paths.setArchived(noteId, archive = true)
    }.isFailure()

    paths.setArchived(noteId, archive = false)
    assertThat(folderPath()).isEqualTo("shows")

    assertThat {
      paths.setArchived(noteId, archive = false)
    }.isFailure()
  }
}
