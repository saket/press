package me.saket.kgit

internal actual class RealGitRepository actual constructor(path: String) : GitRepository {
  override fun addAll(): Unit = TODO()
  override fun commit(message: String, author: GitAuthor): Unit = TODO()
  override fun push(): PushResult = TODO()
}
