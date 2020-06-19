package me.saket.kgit

sealed class RebaseResult {
  object Success : RebaseResult()
  data class Failure(val details: String) : RebaseResult()
}
