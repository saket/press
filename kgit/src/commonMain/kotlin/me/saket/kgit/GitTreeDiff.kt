package me.saket.kgit

import me.saket.kgit.GitTreeDiff.Change

class GitTreeDiff(val changes: List<Change>) : AbstractList<Change>() {
  override val size: Int get() = changes.size
  override fun get(index: Int): Change = changes[index]

  sealed class Change {
    data class Add(val path: String) : Change()
    data class Modify(val path: String) : Change()
    data class Delete(val path: String) : Change()
    data class Copy(val fromPath: String, val toPath: String) : Change()
    data class Rename(val fromPath: String, val toPath: String) : Change()
  }
}
