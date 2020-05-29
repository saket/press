package me.saket.kgit

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode.REBASE
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig.Host
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.util.FS
import java.io.File
import org.eclipse.jgit.api.Git as JGit

internal actual class RealGitRepository actual constructor(
  private val git: Git,
  private val path: String
) : GitRepository {

  private val jgit: JGit by lazy {
    // Initializing a directory that already has git will no-op.
    JGit.init().setDirectory(File(path)).call()
  }

  override fun addAll() {
    jgit.add().addFilepattern(".").call()
  }

  override fun commit(message: String, author: GitAuthor) {
    jgit.commit().apply {
      setAllowEmpty(false)
      setMessage(message)
      setAuthor(author.name, author.email)
    }.call()
  }

  override fun pull(rebase: Boolean): PullResult {
    val pullResult = jgit.pull()
        .apply {
          if (rebase) setRebase(REBASE)
          else TODO()
        }
        .setTransportConfigCallback(sshTransport())
        .call()

    return when {
      pullResult.isSuccessful -> PullResult.Success
      else -> PullResult.Failed(reason = pullResult.toString())
    }
  }

  override fun push(force: Boolean): PushResult {
    val pushResult = jgit.push()
        .setTransportConfigCallback(sshTransport())
        .setForce(force)
        .call()
        .toList()
        .also { check(it.size == 1) { "Did not expect multiple push results: $it" } }
        .single()

    return when (val status = pushResult.getRemoteUpdate("refs/heads/master").status) {
      RemoteRefUpdate.Status.OK -> PushResult.Success
      else -> PushResult.Failed(status.toString())
    }
  }

  private fun sshTransport(): TransportConfigCallback {
    return TransportConfigCallback { transport ->
      if (transport !is SshTransport) return@TransportConfigCallback
      val sshConfig = git.ssh
      requireNotNull(sshConfig)

      transport.sshSessionFactory = object : JschConfigSessionFactory() {
        override fun configure(host: Host, session: Session) = Unit

        override fun createSession(hc: Host?, user: String?, host: String?, port: Int, fs: FS?): Session {
          // JSCH picks up SSH keys from ~/.ssh/config
          // which isn't needed and can potentially fail.
          val emptyHost = Host()
          return super.createSession(emptyHost, user, host, port, fs)
        }

        override fun createDefaultJSch(fs: FS): JSch? {
          return super.createDefaultJSch(fs).apply {
            addIdentity(
                "foo" /* name */,
                sshConfig.privateKey.toByteArray(),
                null  /* public key */,
                sshConfig.passphrase?.toByteArray()
            )
          }
        }
      }
    }
  }

  override fun addRemote(name: String, url: String) {
    jgit.remoteAdd()
        .setName(name)
        .setUri(URIish(url))
        .call()
  }
}
