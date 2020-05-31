package me.saket.kgit

import org.eclipse.jgit.lib.AnyObjectId

actual data class GitSha1(val id: AnyObjectId) {
  actual val value: String get() = id.name

  override fun toString(): String {
    return value.take(7)
  }
}
