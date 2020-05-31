package me.saket.kgit

sealed class GitTreeDiff {
  data class Add(val path: String): GitTreeDiff()
  data class Copy(val fromPath: String, val toPath: String): GitTreeDiff()
  data class Delete(val path: String): GitTreeDiff()
  data class Modify(val path: String): GitTreeDiff()
  data class Rename(val fromPath: String, val toPath: String): GitTreeDiff()
}
