package me.saket.kgit

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import me.saket.kgit.GitTreeDiff.Change.Add
import me.saket.kgit.GitTreeDiff.Change.Copy
import me.saket.kgit.GitTreeDiff.Change.Delete
import me.saket.kgit.GitTreeDiff.Change.Modify
import me.saket.kgit.GitTreeDiff.Change.Rename
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.diff.DiffEntry.ChangeType.ADD
import org.eclipse.jgit.diff.DiffEntry.ChangeType.COPY
import org.eclipse.jgit.diff.DiffEntry.ChangeType.DELETE
import org.eclipse.jgit.diff.DiffEntry.ChangeType.MODIFY
import org.eclipse.jgit.diff.DiffEntry.ChangeType.RENAME
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode.REBASE
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.UserConfig
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig.Host
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
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
    val pullResult = jgit.pull()
        .apply {
          if (rebase) setRebase(REBASE)
          else TODO()
        }
        .setTransportConfigCallback(sshTransport())
        .call()

    pullResult.rebaseResult?.run {
      println("Rebase result: $status")
      if (conflicts != null) println("Conflicts: $conflicts")
      if (failingPaths != null) println("Failing paths: $failingPaths")
      if (uncommittedChanges != null) println("Uncommitted changes: $uncommittedChanges")
    }

    return when {
      pullResult.isSuccessful -> PullResult.Success
      else -> PullResult.Failure(reason = pullResult.toString())
    }
  }

//  private fun printLog() {
//    println("Files: ${File(directoryPath).listFiles()!!.map { it.name }}")
//    for (log in jgit.log().call()) {
//      println("${log.name.take(7)} - ${log.fullMessage}")
//    }
//  }

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

  override fun resolve(revision: String): GitSha1? {
    val resolvedId = jgit.repository.resolve(revision)

    if (revision == "HEAD" && resolvedId == null) {
      return GitSha1(jgit.repository.resolve("4b825dc642cb6eb9a060e54bf8d69288fbee4904"))
    }

    return resolvedId?.let(::GitSha1)
  }

  @OptIn(ExperimentalStdlibApi::class)
  override fun commitsBetween(from: GitSha1?, to: GitSha1): List<GitCommit> {
    RevWalk(jgit.repository).use { walk ->
      val startCommit: RevCommit = walk.parseCommit(to.id)
      walk.markStart(startCommit)

      val commits = buildList {
        for (commit in walk) {
          add(GitCommit(commit))
          if (from != null && commit.id == from.id) {
            break
          }
        }
      }
      walk.dispose()
      return commits.reversed()
    }
  }

  override fun diffBetween(first: GitCommit?, second: GitCommit): GitTreeDiff {
    val firstTree = first?.commit?.tree
    val secondTree = second.commit.tree

    jgit.repository.newObjectReader().use { reader ->
      val firstTreeParser = when {
        firstTree != null -> CanonicalTreeParser().apply { reset(reader, firstTree.id) }
        else -> EmptyTreeIterator()
      }
      val secondTreeParser = CanonicalTreeParser().apply { reset(reader, secondTree.id) }

      val diffEntries = jgit.diff()
          .setNewTree(secondTreeParser)
          .setOldTree(firstTreeParser)
          .setShowNameAndStatusOnly(true)
          .call()

      return GitTreeDiff(diffEntries.map {
        when (it.changeType!!) {
          ADD -> Add(path = it.newPath)
          MODIFY -> Modify(path = it.oldPath)
          COPY -> Copy(fromPath = it.oldPath, toPath = it.newPath)
          DELETE -> Delete(path = it.oldPath)
          RENAME -> Rename(fromPath = it.oldPath, toPath = it.newPath)
        }
      })
    }
  }
}
