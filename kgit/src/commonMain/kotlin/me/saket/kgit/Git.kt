package me.saket.kgit

interface Git {
  fun repository(sshKey: SshPrivateKey, path: String): GitRepository
}

class RealGit : Git {
  override fun repository(sshKey: SshPrivateKey, path: String): GitRepository {
    return RealGitRepository(path, sshKey)
  }
}
