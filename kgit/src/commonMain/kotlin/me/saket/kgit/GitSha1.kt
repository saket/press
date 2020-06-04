package me.saket.kgit

expect class GitSha1 {
  val value: String
}

val GitSha1.abbreviated: String
  get() = value.take(7)
