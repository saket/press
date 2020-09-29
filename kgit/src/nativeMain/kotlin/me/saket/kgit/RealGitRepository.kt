package me.saket.kgit

internal actual class RealGitRepository actual constructor(
  directoryPath: String,
  remote: GitRemote,
  userConfig: GitConfig,
  sshKey: SshPrivateKey
) : GitRepository {
  override fun isStagingAreaDirty(): Boolean = TODO()
  override fun checkout(branch: String, createIfNeeded: Boolean): Unit = TODO()
  override fun checkout(commit: GitCommit): Unit = TODO()
  override fun commitAll(message: String, timestamp: UtcTimestamp, allowEmpty: Boolean) = TODO()
  override fun pull(rebase: Boolean): GitPullResult = TODO()
  override fun push(force: Boolean): PushResult = TODO()
  override fun hardResetTo(sha1: String, resetState: Boolean, deleteUntrackedFiles: Boolean): Unit = TODO()
  override fun headCommit(onBranch: String?): GitCommit = TODO()
  override fun commitsBetween(from: GitCommit?, toInclusive: GitCommit): List<GitCommit> = TODO()
  override fun commonAncestor(first: GitCommit, second: GitCommit): GitCommit? = TODO()
  override fun changesBetween(from: GitCommit?, to: GitCommit): GitTreeDiff = TODO()
  override fun currentBranch(): GitBranch = TODO()
}

actual fun Git.Companion.identify(e: Throwable): GitError {
  TODO()
}
