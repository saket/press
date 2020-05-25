package me.saket.kgit

object Git {
  fun repository(path: String): GitRepository {
    return RealGitRepository(path)
  }
}
