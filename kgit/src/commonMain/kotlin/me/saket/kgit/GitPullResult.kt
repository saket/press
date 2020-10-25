package me.saket.kgit

sealed class GitPullResult {
  object Success : GitPullResult()
  object NonExistentBranch : GitPullResult()
  data class Failure(val reason: String) : GitPullResult()
}
