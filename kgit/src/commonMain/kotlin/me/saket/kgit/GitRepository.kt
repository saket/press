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
    timestamp: UtcTimestamp? = null
  )

  abstract fun pull(rebase: Boolean): PullResult

  abstract fun push(force: Boolean = false): PushResult

  abstract fun addRemote(name: String, url: String)

  abstract fun resolve(revision: String): GitSha1?

  /**
   * When [from] is null, a list of all commits till [to] are returned.
   */
  abstract fun commitsBetween(from: GitSha1?, to: GitSha1): List<GitCommit>

  /**
   * When [first] is null, the [second] commit's tree is compared with an empty tree.
   */
  abstract fun diffBetween(first: GitCommit?, second: GitCommit): GitTreeDiff
}
