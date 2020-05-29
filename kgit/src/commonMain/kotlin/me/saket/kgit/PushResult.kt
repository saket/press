package me.saket.kgit

sealed class PushResult {
  object Success : PushResult()
  object AlreadyUpToDate : PushResult()
  data class Failure(val reason: String) : PushResult()
}
