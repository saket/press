package me.saket.press.shared.syncer.git

import me.saket.kgit.Git
import me.saket.kgit.GitConfig
import me.saket.kgit.GitPullResult
import me.saket.kgit.GitRepository
import me.saket.kgit.PushResult
import me.saket.kgit.SshPrivateKey
import me.saket.kgit.UtcTimestamp

class DelegatingGit(private val delegate: Git) : Git {
  lateinit var repository: GitRepository
  val prePulls = mutableListOf<() -> Unit>()
  val preCommits = mutableListOf<(message: String) -> Unit>()
  val prePushes = mutableListOf<() -> Unit>()
  val postPushes = mutableListOf<() -> Unit>()
  var postHardReset = {}
  var pushCount = 0

  override fun repository(path: String, sshKey: SshPrivateKey, remoteSshUrl: String, userConfig: GitConfig) =
    DelegatingGitRepository(this, delegate.repository(path, sshKey, remoteSshUrl, userConfig)).also {
      repository = it
    }

  class DelegatingGitRepository(
    private val git: DelegatingGit,
    private val delegate: GitRepository
  ) : GitRepository by delegate {

    override fun pull(rebase: Boolean): GitPullResult {
      git.prePulls.forEach { it() }
      return delegate.pull(rebase)
    }

    override fun commitAll(message: String, timestamp: UtcTimestamp, allowEmpty: Boolean) {
      git.preCommits.forEach { it(message) }
      delegate.commitAll(message, timestamp, allowEmpty)
    }

    override fun push(force: Boolean): PushResult {
      git.prePushes.forEach { it() }
      git.pushCount++
      return delegate.push(force).also {
        git.postPushes.forEach { it() }
      }
    }

    override fun hardResetTo(sha1: String, resetState: Boolean, deleteUntrackedFiles: Boolean) {
      delegate.hardResetTo(sha1, resetState, deleteUntrackedFiles)
      git.postHardReset()
    }
  }
}
