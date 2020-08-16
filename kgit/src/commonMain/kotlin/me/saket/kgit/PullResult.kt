package me.saket.kgit

sealed class PullResult {
  object Success : PullResult()
  data class Failure(val reason: String, val abort: () -> Unit) : PullResult()
}
