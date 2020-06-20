package me.saket.kgit

internal expect class RealGitRepository(git: Git, directoryPath: String) : GitRepository

abstract class GitRepository(open val directoryPath: String) {
  // See usage in JVM for explanation.
  open var workaroundJgitBug: Boolean = false

  abstract fun isStagingAreaDirty(): Boolean

  /**
   * Add all files to staging and commit.
   *
   * @param author when null, the author information is taken from repository's config.
   * @param timestamp when null, the current time is used.
   */
  abstract fun commitAll(
    message: String,
    author: GitAuthor? = null,
    timestamp: UtcTimestamp? = null,
    allowEmpty: Boolean = false
  )

  abstract fun pull(rebase: Boolean): PullResult

  abstract fun fetch()

  /**
   * Find files which will fail to merge if the current head is merged/rebased with a
   * commit. Press uses this for resolving conflicts before rebasing with upstream.
   */
  abstract fun mergeConflicts(with: GitCommit): List<MergeConflict>

  abstract fun rebase(with: GitCommit, strategy: MergeStrategy): RebaseResult

  abstract fun push(force: Boolean = false): PushResult

  abstract fun addRemote(name: String, url: String)

  /**
   * The commit HEAD is pointing to on [onBranch].
   * When [onBranch] is null, the current branch is used.
   */
  abstract fun headCommit(onBranch: String? = null): GitCommit?

  /**
   * When [from] is null, a list of all commits till [toInclusive] are returned.
   */
  abstract fun commitsBetween(from: GitCommit?, toInclusive: GitCommit): List<GitCommit>

  abstract fun commonAncestor(first: GitCommit, second: GitCommit): GitCommit?

  /**
   * When [from] is null, the [to] commit's tree is compared with an empty tree.
   */
  abstract fun diffBetween(from: GitCommit?, to: GitCommit): GitTreeDiff

  abstract fun currentBranch(): GitBranch
}
