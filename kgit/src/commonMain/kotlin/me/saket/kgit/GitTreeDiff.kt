package me.saket.kgit

import me.saket.kgit.GitTreeDiff.Change

class GitTreeDiff(private val changes: List<Change>) : AbstractList<Change>() {
  override val size: Int get() = changes.size
  override fun get(index: Int): Change = changes[index]

  /** @param path Last known path of the changed file. */
  sealed class Change(open val path: String) {
    data class Add(override val path: String) : Change(path)
    data class Modify(override val path: String) : Change(path)
    data class Delete(override val path: String) : Change(path)
    data class Copy(val fromPath: String, val toPath: String) : Change(toPath)
    data class Rename(val fromPath: String, val toPath: String) : Change(toPath)
  }
}
