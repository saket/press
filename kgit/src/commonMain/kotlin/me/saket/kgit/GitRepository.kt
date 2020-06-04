package me.saket.kgit

internal expect class RealGitRepository(git: Git, directoryPath: String) : GitRepository

abstract class GitRepository(open val directoryPath: String) {
  /** git add . */
  abstract fun addAll()

  /**
   * @param author when null, the author information is taken from repository's config.
   * @param timestamp when null, the current time is used.
   */
  abstract fun commit(
    message: String,
    author: GitAuthor? = null,
    timestamp: UtcTimestamp? = null,
    allowEmpty: Boolean = false
  )

  abstract fun pull(rebase: Boolean): PullResult

  abstract fun fetch()

  abstract fun rebase(with: GitCommit): RebaseResult

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
