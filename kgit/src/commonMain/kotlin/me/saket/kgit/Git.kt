package me.saket.kgit

object Git {
  fun repository(
    path: String,
    sshKey: SshPrivateKey,
    remoteSshUrl: String,
    userConfig: GitConfig = GitConfig()
  ): GitRepository = RealGitRepository(path, userConfig, GitRemote("origin", remoteSshUrl), sshKey)
}
