package me.saket.kgit

interface Git {
  fun repository(
    path: String,
    sshKey: SshPrivateKey,
    remoteSshUrl: String,
    userConfig: GitConfig = GitConfig()
  ): GitRepository

  companion object
}

class RealGit : Git {
  override fun repository(
    path: String,
    sshKey: SshPrivateKey,
    remoteSshUrl: String,
    userConfig: GitConfig
  ): GitRepository = RealGitRepository(path, GitRemote("origin", remoteSshUrl), userConfig, sshKey)
}

expect fun Git.Companion.tryRecovering(e: Throwable): GitErrorRecoveryResult
