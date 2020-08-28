package me.saket.kgit

interface Git {
  fun repository(
    path: String,
    sshKey: SshPrivateKey,
    remoteSshUrl: String,
    userConfig: GitConfig = GitConfig(),
    author: GitAuthor
  ): GitRepository

  companion object
}

class RealGit : Git {
  override fun repository(
    path: String,
    sshKey: SshPrivateKey,
    remoteSshUrl: String,
    userConfig: GitConfig,
    author: GitAuthor
  ): GitRepository = RealGitRepository(path, userConfig, GitRemote("origin", remoteSshUrl), sshKey, author)
}

expect fun Git.Companion.isKnownError(e: Throwable): Boolean
