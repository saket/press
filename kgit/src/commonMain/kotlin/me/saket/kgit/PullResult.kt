package me.saket.kgit

sealed class PullResult {
  object Success : PullResult()
  data class Failed(val reason: String) : PullResult()
}
