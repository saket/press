package me.saket.kgit

internal expect class RealGitRepository(path: String) : GitRepository

interface GitRepository {
  /** git add . */
  fun addAll()

  fun commit(message: String, author: GitAuthor)

  fun push(sshPrivateKey: String): PushResult

  fun addRemote(name: String, url: String)
}
