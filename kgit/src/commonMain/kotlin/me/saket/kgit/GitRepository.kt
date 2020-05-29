package me.saket.kgit

internal expect class RealGitRepository(git: Git, path: String) : GitRepository

interface GitRepository {
  /** git add . */
  fun addAll()

  fun commit(message: String, author: GitAuthor? = null)

  fun pull(rebase: Boolean): PullResult

  fun push(force: Boolean = false): PushResult

  fun addRemote(name: String, url: String)
}
