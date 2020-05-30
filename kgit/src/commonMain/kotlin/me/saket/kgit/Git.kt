package me.saket.kgit

import co.touchlab.stately.concurrency.AtomicReference

interface Git {
  var ssh: SshConfig?
  fun repository(path: String): GitRepository
}

class RealGit : Git {
  private var _ssh = AtomicReference<SshConfig?>(null)
  override var ssh: SshConfig?
    get() = _ssh.get()
    set(value) = _ssh.set(value)

  override fun repository(path: String): GitRepository {
    return RealGitRepository(this, path)
  }
}
