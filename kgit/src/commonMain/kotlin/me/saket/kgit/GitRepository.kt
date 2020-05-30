package me.saket.kgit

internal expect class RealGitRepository(git: Git, directoryPath: String) : GitRepository

abstract class GitRepository(open val directoryPath: String) {
  /** git add . */
  abstract fun addAll()

  abstract fun commit(message: String, author: GitAuthor? = null)

  abstract fun pull(rebase: Boolean): PullResult

  abstract fun push(force: Boolean = false): PushResult

  abstract fun addRemote(name: String, url: String)

  abstract fun resolve(revision: String): GitSha1

  abstract fun diff(first: GitSha1, second: GitSha1)
}
