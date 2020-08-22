package me.saket.kgit

internal actual class RealGitRepository actual constructor(
  directoryPath: String,
  userConfig: GitConfig,
  remote: GitRemote,
  sshKey: SshPrivateKey
) : GitRepository {
  override fun isStagingAreaDirty(): Boolean = TODO()
  override fun checkout(branch: String, create: Boolean): Unit = TODO()
  override fun commitAll(message: String, author: GitAuthor?, timestamp: UtcTimestamp?, allowEmpty: Boolean) = TODO()
  override fun pull(rebase: Boolean): GitPullResult = TODO()
  override fun push(force: Boolean): PushResult = TODO()
  override fun hardResetTo(commit: GitCommit): Unit = TODO()
  override fun headCommit(onBranch: String?): GitCommit = TODO()
  override fun commitsBetween(from: GitCommit?, toInclusive: GitCommit): List<GitCommit> = TODO()
  override fun commonAncestor(first: GitCommit, second: GitCommit): GitCommit? = TODO()
  override fun changesIn(commit: GitCommit): GitTreeDiff = TODO()
  override fun diffBetween(from: GitCommit?, to: GitCommit): GitTreeDiff = TODO()
  override fun currentBranch(): GitBranch = TODO()
}

actual fun Git.Companion.isKnownError(e: Throwable): Boolean {
  TODO()
}
