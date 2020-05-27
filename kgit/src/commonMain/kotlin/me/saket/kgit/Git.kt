package me.saket.kgit

interface Git {
  var ssh: SshConfig?

  fun repository(path: String): GitRepository {
    return RealGitRepository(this, path)
  }
}

class RealGit : Git {
  // todo: use AtomicReference instead.
  private var _ssh: SshConfig? = null
  override var ssh: SshConfig?
    get() = _ssh
    set(value) {
      _ssh = value
    }
}
