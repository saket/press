package me.saket.kgit

expect class GitCommit {
  val sha1: GitSha1
  val message: String
  val author: GitAuthor
  val timestamp: UtcTimestamp
}
