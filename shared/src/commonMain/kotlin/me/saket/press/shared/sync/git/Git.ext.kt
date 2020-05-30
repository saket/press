package me.saket.press.shared.sync.git

import me.saket.kgit.Git
import me.saket.kgit.GitRepository

fun Git.repository(directory: File): GitRepository {
  return repository(directory.path)
}
