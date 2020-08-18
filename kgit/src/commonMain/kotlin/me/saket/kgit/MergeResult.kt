package me.saket.kgit

sealed class MergeResult {
  object Success : MergeResult()
  data class Failure(val reason: String, val abort: () -> Unit) : MergeResult()
}
