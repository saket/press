package me.saket.kgit

enum class GitErrorRecoveryResult {
  Recovered,

  NetworkError,

  /** Likely means that the ssh-key wasn't authorized to access the repository. */
  AuthFailed,

  UnknownError,
}
