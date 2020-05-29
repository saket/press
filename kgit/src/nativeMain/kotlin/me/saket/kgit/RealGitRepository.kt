package me.saket.kgit

internal actual class RealGitRepository actual constructor(git: Git, path: String) : GitRepository {
  override fun addAll() = TODO()
  override fun commit(message: String, author: GitAuthor?) = TODO()
  override fun push(force: Boolean): PushResult = TODO()
  override fun addRemote(name: String, url: String) = TODO()
  override fun pull(rebase: Boolean): PullResult = TODO()
}
