package me.saket.kgit

internal actual class RealGitRepository actual constructor(
  git: Git,
  directoryPath: String
) : GitRepository(directoryPath) {
  override fun addAll() = TODO()
  override fun commit(message: String, author: GitAuthor?, timestamp: UtcTimestamp) = TODO()
  override fun pull(rebase: Boolean): PullResult = TODO()
  override fun push(force: Boolean): PushResult = TODO()
  override fun addRemote(name: String, url: String) = TODO()
  override fun resolve(revision: String): GitSha1? = TODO()
  override fun diff(first: GitSha1?, second: GitSha1): Unit = TODO()
}
