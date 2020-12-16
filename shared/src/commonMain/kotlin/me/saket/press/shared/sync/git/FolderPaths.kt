package me.saket.press.shared.sync.git

import me.saket.press.PressDatabase
import me.saket.press.data.shared.Folder
import me.saket.press.data.shared.FolderQueries
import me.saket.press.shared.db.FolderId

@OptIn(ExperimentalStdlibApi::class)
class FolderPaths(private val database: PressDatabase) {
  private val queries get() = database.folderQueries

  /**
   * Path of a [FolderId], relative to the root notes directory.
   */
  fun createPath(
    id: FolderId,
    existingFolders: List<Folder> = database.folderQueries.allFolders().executeAsList()
  ): String {
    return buildList {
      val idsToFolders = existingFolders.associateBy { it.id }

      var parentId: FolderId? = id
      while (parentId != null) {
        val parent = idsToFolders[parentId]!!
        add(parent.name)
        parentId = parent.parent
      }
    }.reversed().joinToString(separator = "/")
  }

  /**
   * @param folderPath path/to/a/folder
   * @return FolderId that represents [folderPath].
   */
  fun mkdirs(folderPath: String): FolderId? {
    if (folderPath.isBlank()) {
      return null
    }

    val allFolders = queries.allFolders().executeAsList()
    val pathsToFolders = allFolders
      .associateBy { createPath(id = it.id, allFolders) }
      .toMutableMap()

    var nextPath = ""
    var nextParent: FolderId? = null

    for (folder in folderPath.split("/")) {
      nextPath += folder

      if (nextPath !in pathsToFolders) {
        val folderId = FolderId.generate()
        queries.insert(id = folderId, name = folder, parent = nextParent)
        pathsToFolders[nextPath] = Folder(localId = -1, id = folderId, name = folder, parent = nextParent)
      }

      nextParent = pathsToFolders[nextPath]!!.id
      nextPath += "/"
    }

    return pathsToFolders[folderPath]!!.id
  }
}

fun FolderQueries.testInsert(vararg folders: Folder) {
  folders.forEach { testInsert(it) }
}
