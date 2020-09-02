package me.saket.kgit

sealed class GitError {
  object NetworkError : GitError()

  /** Likely means that the ssh-key wasn't authorized to access the repository. */
  object AuthFailed : GitError()

  object Unknown : GitError()
}
