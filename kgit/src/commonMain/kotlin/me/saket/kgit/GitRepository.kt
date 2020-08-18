package me.saket.kgit

internal expect class RealGitRepository(
  directoryPath: String,
  userConfig: GitConfig,
  remote: GitRemote,
  sshKey: SshPrivateKey
) : GitRepository

interface GitRepository {

  fun isStagingAreaDirty(): Boolean

  fun checkout(branch: String, create: Boolean = true)

  /**
   * Add all files to staging and commit.
   *
   * @param author when null, the author information is taken from repository's config.
   * @param timestamp when null, the current time is used.
   */
  fun commitAll(
    message: String,
    author: GitAuthor? = null,
    timestamp: UtcTimestamp? = null,
    allowEmpty: Boolean = false
  )

  fun pull(rebase: Boolean): GitPullResult

  fun merge(with: GitCommit): MergeResult

  /**
   * Find files which will fail to merge if the current head is merged/rebased with a
   * commit. Press uses this for resolving conflicts before rebasing with upstream.
   */
  fun mergeConflicts(with: GitCommit): List<MergeConflict>

  fun rebase(with: GitCommit, strategy: MergeStrategy): RebaseResult

  fun push(force: Boolean = false): PushResult

  fun hardResetTo(commit: GitCommit)

  /**
   * The commit HEAD is pointing to on [onBranch].
   * When [onBranch] is null, the current branch is used.
   */
  fun headCommit(onBranch: String? = null): GitCommit?

  /**
   * When [from] is null, a list of all commits till [toInclusive] are returned.
   */
  fun commitsBetween(from: GitCommit?, toInclusive: GitCommit): List<GitCommit>

  fun commonAncestor(first: GitCommit, second: GitCommit): GitCommit?

  /**
   * Diff between [commit] and its parent.
   */
  fun changesIn(commit: GitCommit): GitTreeDiff

  /**
   * When [from] is null, the [to] commit's tree is compared with an empty tree.
   */
  fun diffBetween(from: GitCommit?, to: GitCommit): GitTreeDiff

  fun currentBranch(): GitBranch
}
