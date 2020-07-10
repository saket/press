package me.saket.kgit

internal actual class RealGitRepository actual constructor(
  directoryPath: String,
  sshKey: SshPrivateKey
) : GitRepository {
  override fun resetUserConfigTo(config: GitConfig): Unit = TODO()
  override fun isStagingAreaDirty(): Boolean = TODO()
  override fun checkout(branch: String, create: Boolean): Unit = TODO()
  override fun commitAll(message: String, author: GitAuthor?, timestamp: UtcTimestamp?, allowEmpty: Boolean) = TODO()
  override fun pull(rebase: Boolean): PullResult = TODO()
  override fun fetch(): Unit = TODO()
  override fun mergeConflicts(with: GitCommit): List<MergeConflict> = TODO()
  override fun rebase(with: GitCommit, strategy: MergeStrategy): RebaseResult = TODO()
  override fun push(force: Boolean): PushResult = TODO()
  override fun addRemote(name: String, url: String) = TODO()
  override fun headCommit(onBranch: String?): GitCommit = TODO()
  override fun commitsBetween(from: GitCommit?, toInclusive: GitCommit): List<GitCommit> = TODO()
  override fun commonAncestor(first: GitCommit, second: GitCommit): GitCommit? = TODO()
  override fun changesIn(commit: GitCommit): GitTreeDiff = TODO()
  override fun diffBetween(from: GitCommit?, to: GitCommit): GitTreeDiff = TODO()
  override fun currentBranch(): GitBranch = TODO()
}
