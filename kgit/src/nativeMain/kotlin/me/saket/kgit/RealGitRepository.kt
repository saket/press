package me.saket.kgit

internal actual class RealGitRepository actual constructor(path: String) : GitRepository {
  override fun addAll() = TODO()
  override fun commit(message: String, author: GitAuthor) = TODO()
  override fun push(sshPrivateKey: String): PushResult = TODO()
  override fun addRemote(name: String, url: String) = TODO()
}
