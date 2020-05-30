package me.saket.kgit

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode.REBASE
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.UserConfig
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig.Host
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.util.FS
import java.io.File
import java.util.Date
import java.util.TimeZone
import org.eclipse.jgit.api.Git as JGit

internal actual class RealGitRepository actual constructor(
  private val git: Git,
  override val directoryPath: String
) : GitRepository(directoryPath) {

  private val jgit: JGit by lazy {
    // Initializing a directory that already has git will no-op.
    JGit.init().setDirectory(File(directoryPath)).call()
  }

  override fun addAll() {
    jgit.add().addFilepattern(".").call()
  }

  @Suppress("NAME_SHADOWING")
  override fun commit(message: String, author: GitAuthor?, timestamp: UtcTimestamp?) {
    jgit.commit().apply {
      setAllowEmpty(false)
      setMessage(message)

      setAuthor(timestamp?.let {
        val author = author ?: defaultCommitAuthor()
        PersonIdent(author.name, author.email, Date(it.millis), TimeZone.getTimeZone("UTC"))
      })
    }.call()
  }

  private fun defaultCommitAuthor(): GitAuthor {
    val config = jgit.repository.config.get(UserConfig.KEY)
    return GitAuthor(name = config.authorName, email = config.authorEmail)
  }

  override fun pull(rebase: Boolean): PullResult {
    println("Before pulling, head is at ${resolve("head").sha1.take(7)}")
    printLog()

    val pullResult = jgit.pull()
        .apply {
          if (rebase) setRebase(REBASE)
          else TODO()
        }
        .setTransportConfigCallback(sshTransport())
        .call()

    println("\nAfter pulling, head is at ${resolve("head").sha1.take(7)}")
    printLog()

    pullResult.rebaseResult?.run {
      println("\nRebase result: $status")
      println("Conflicts: $conflicts")
      println("Failing paths: $failingPaths")
      println("Uncommitted changes: $uncommittedChanges")
      println()
    }

    return when {
      pullResult.isSuccessful -> PullResult.Success
      else -> PullResult.Failure(reason = pullResult.toString())
    }
  }

  private fun printLog() {
    println("Files: ${File(directoryPath).listFiles()!!.map { it.name }}")
    for (log in jgit.log().call()) {
      println("${log.name.take(7)} - ${log.fullMessage}")
    }
    files()
  }

  fun files() {
    println("Files on head:")
    jgit.repository.use { repository ->
      val head: Ref = repository.findRef("HEAD")
      RevWalk(repository).use { walk ->
        val commit: RevCommit = walk.parseCommit(head.objectId)
        TreeWalk(repository).use { treeWalk ->
          treeWalk.addTree(commit.tree)
          treeWalk.isRecursive = true
          while (treeWalk.next()) {
            println("found: " + treeWalk.pathString)
          }
        }
      }
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
      RemoteRefUpdate.Status.UP_TO_DATE -> PushResult.AlreadyUpToDate
      else -> PushResult.Failure(status.toString())
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

  override fun resolve(revision: String): GitSha1 {
    val resolvedId = jgit.repository.resolve(revision)
    require(resolvedId is AnyObjectId) { "Unknown kind of ObjectId: $resolvedId" }
    return GitSha1(resolvedId)
  }

  override fun diff(first: GitSha1, second: GitSha1) {
    jgit.repository.newObjectReader().use { reader ->
      val firstTreeParser = CanonicalTreeParser().apply { reset(reader, first.id) }
      val secondTreeParser = CanonicalTreeParser().apply { reset(reader, second.id) }

      val diffEntries = jgit.diff()
          .setNewTree(secondTreeParser)
          .setOldTree(firstTreeParser)
          .setShowNameAndStatusOnly(true)
          .call()
      for (entry in diffEntries) {
        println("Entry: $entry")
      }
    }
  }
}
