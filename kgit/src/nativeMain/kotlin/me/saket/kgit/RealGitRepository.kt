package me.saket.kgit

internal actual class RealGitRepository actual constructor(
  git: Git,
  directoryPath: String
) : GitRepository(directoryPath) {
  override fun addAll() = TODO()
  override fun commit(message: String, author: GitAuthor?, timestamp: UtcTimestamp?, allowEmpty: Boolean) = TODO()
  override fun pull(rebase: Boolean): PullResult = TODO()
  override fun fetch(): Unit = TODO()
  override fun checkout(branch: String, create: Boolean): Unit = TODO()
  override fun rebase(with: GitCommit): RebaseResult = TODO()
  override fun push(force: Boolean): PushResult = TODO()
  override fun addRemote(name: String, url: String) = TODO()
  override fun headCommit(): GitCommit = TODO()
  override fun commitsBetween(from: GitCommit?, toInclusive: GitCommit): List<GitCommit> = TODO()
  override fun diffBetween(from: GitCommit?, to: GitCommit): GitTreeDiff = TODO()
  override fun currentBranch(): GitBranch = TODO()
}
