package me.saket.kgit

enum class GitErrorRecoveryResult {
  Recovered,

  NetworkError,

  /**
   * Likely means that the ssh-key wasn't authorized to access the repository
   * or an old RSA key was previously deployed, which isn't supported by GitHub anymore.
   */
  AuthFailed,

  UnknownError,
}
