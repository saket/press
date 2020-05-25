package me.saket.kgit

sealed class PushResult {
  object Success: PushResult()
  data class Failed(val reason: String): PushResult()
}
