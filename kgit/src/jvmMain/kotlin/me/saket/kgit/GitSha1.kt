package me.saket.kgit

import org.eclipse.jgit.lib.AnyObjectId

actual class GitSha1(val id: AnyObjectId) {
  actual val sha1: String get() = id.name

  override fun toString(): String {
    return "GitSha1[$sha1]"
  }
}
