package me.saket.kgit

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.diff.DiffEntry
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
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
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
    //println("Before pulling, head is at ${resolve("head")?.sha1?.take(7)}")
    //printLog()

    val pullResult = jgit.pull()
        .apply {
          if (rebase) setRebase(REBASE)
          else TODO()
        }
        .setTransportConfigCallback(sshTransport())
        .call()

    //println("\nAfter pulling, head is at ${resolve("head")?.sha1?.take(7)}")
    //printLog()

    pullResult.rebaseResult?.run {
      println("Rebase result: $status")
      if (conflicts != null) println("Conflicts: $conflicts")
      if (failingPaths != null) println("Failing paths: $failingPaths")
      if (uncommittedChanges != null) println("Uncommitted changes: $uncommittedChanges")
      println()
    }

    return when {
      pullResult.isSuccessful -> PullResult.Success
      else -> PullResult.Failure(reason = pullResult.toString())
    }
  }

  private fun printLog() {
    try {
      println("Files: ${File(directoryPath).listFiles()!!.map { it.name }}")
      for (log in jgit.log().call()) {
        println("${log.name.take(7)} - ${log.fullMessage}")
      }
      files()
    } catch (ignored: Throwable) {
    }
  }

  private fun files() {
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

  override fun resolve(revision: String): GitSha1? {
    val resolvedId = jgit.repository.resolve(revision)

    if (revision == "HEAD" && resolvedId == null) {
      return GitSha1(jgit.repository.resolve("4b825dc642cb6eb9a060e54bf8d69288fbee4904"))
    }

    return resolvedId?.let(::GitSha1)
  }

  @OptIn(ExperimentalStdlibApi::class)
  override fun diff(first: GitSha1?, second: GitSha1) {
    val to = second.sha1

    // a RevWalk allows to walk over commits based on some filtering that is defined
    RevWalk(jgit.repository).use { walk ->
      val startCommit: RevCommit = walk.parseCommit(second.id)
      //println("Start-Commit: $startCommit")
      println("Walking all commits starting at $to until we find $first\n")
      walk.markStart(startCommit)
      val commits = buildList<RevCommit> {
        for (commit in walk) {
          add(commit)
          if (first != null && commit.id == first.id) {
            break
          }
        }
      }.reversed()
      walk.dispose()

      commits.forEach {
        println("${it.id.name} - ${it.shortMessage} (${it.authorIdent.`when`.time})")
      }

      require(commits.size >= 2) { "can't diff with just one commit" }

      println("\nFinding diffs for each commit:")

      // todo: use commits.zipWithNext() instead of looking up the previous commit everytime.
      commits.forEach { commit ->
        val previousTreeId = previousCommitFor(commit)?.tree?.id?.let(::GitSha1)
        diffForReal(firstTree = previousTreeId, secondTree = GitSha1(commit.tree.id))
      }
    }
  }

  private fun previousCommitFor(commit: RevCommit): RevCommit? {
    RevWalk(jgit.repository).use { walk ->
      // TODO: JGit flags a commit as seen so it won't show up again,
      //  requiring another parseCommit(). Might be able to use walk.reset().
      walk.markStart(walk.parseCommit(commit.id))

      for ((count, rev) in walk.withIndex()) {
        if (count == 1) {
          walk.dispose()
          return rev
        }
      }

      walk.dispose()
    }
    return null
  }

  private fun diffForReal(firstTree: GitSha1?, secondTree: GitSha1) {
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
      println("${diffEntries.size} diff entries b/w $firstTree and $secondTree")
      for (entry in diffEntries) {
        println("Entry: $entry")
      }
    }
  }
}
