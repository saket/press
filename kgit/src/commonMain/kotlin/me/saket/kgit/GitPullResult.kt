package me.saket.kgit

sealed class GitPullResult {
  object Success : GitPullResult()
  data class Failure(val reason: String) : GitPullResult()
}
