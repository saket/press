package me.saket.press.shared.sync

import me.saket.kgit.Git
import me.saket.kgit.GitConfig
import me.saket.kgit.GitRepository
import me.saket.kgit.PushResult
import me.saket.kgit.RealGit
import me.saket.kgit.SshPrivateKey

class DelegatingGit(private val delegate: Git) : Git {
  var pushResult: PushResult? = null
  var pushCount = 0

  override fun repository(
    path: String,
    sshKey: SshPrivateKey,
    remoteSshUrl: String,
    userConfig: GitConfig
  ): GitRepository {
    return DelegatingGitRepository(this, delegate.repository(path, sshKey, remoteSshUrl, userConfig))
  }

  class DelegatingGitRepository(
    private val git: DelegatingGit,
    private val delegate: GitRepository
  ) : GitRepository by delegate {

    override fun push(force: Boolean): PushResult {
      git.pushCount++
      return git.pushResult ?: delegate.push(force)
    }
  }
}
