package me.saket.kgit

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import me.saket.kgit.GitTreeDiff.Change.Add
import me.saket.kgit.GitTreeDiff.Change.Copy
import me.saket.kgit.GitTreeDiff.Change.Delete
import me.saket.kgit.GitTreeDiff.Change.Modify
import me.saket.kgit.GitTreeDiff.Change.Rename
import me.saket.kgit.MergeConflict.TheirContent
import me.saket.kgit.MergeStrategy.OURS
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM
import org.eclipse.jgit.api.MergeCommand.FastForwardMode.FF
import org.eclipse.jgit.api.RebaseCommand.Operation.ABORT
import org.eclipse.jgit.api.RebaseResult.Status.STOPPED
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.diff.DiffEntry.ChangeType.ADD
import org.eclipse.jgit.diff.DiffEntry.ChangeType.COPY
import org.eclipse.jgit.diff.DiffEntry.ChangeType.DELETE
import org.eclipse.jgit.diff.DiffEntry.ChangeType.MODIFY
import org.eclipse.jgit.diff.DiffEntry.ChangeType.RENAME
import org.eclipse.jgit.lib.BranchConfig.BranchRebaseMode.REBASE
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.ObjectLoader
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.UserConfig
import org.eclipse.jgit.merge.ResolveMerger
import org.eclipse.jgit.merge.ThreeWayMergeStrategy
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig.Host
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.EmptyTreeIterator
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import org.eclipse.jgit.util.FS
import java.time.Duration
import java.util.Date
import java.util.TimeZone
import kotlin.LazyThreadSafetyMode.NONE
import org.eclipse.jgit.api.Git as JGit
import org.eclipse.jgit.lib.Repository as JRepository
import org.eclipse.jgit.merge.MergeStrategy as JgitMergeStrategy
import org.eclipse.jgit.util.SystemReader as JgitSystemReader
import java.io.File as JavaFile

/**
 * JGit is garbage. Try replacing it with [github.com/git24j/git24j].
 */
internal actual class RealGitRepository actual constructor(
  directoryPath: String,
  private val sshKey: SshPrivateKey
) : GitRepository {

  private val directory = JavaFile(directoryPath)
  private val jgit: JGit by lazy(NONE) {
    JGit.init().setDirectory(directory).call()
  }

  override fun maybeInit(config: () -> GitConfig) {
    if (JavaFile(directory, ".git").exists()) {
      return
    }

    JGit.init().setDirectory(directory).call()

    // Note to self: if this doesn't work, try using something
    // from https://stackoverflow.com/q/33804097/2511884.
    val userConfig = JgitSystemReader.getInstance().userConfig
    userConfig.clear()

    val repoConfig = jgit.repository.config
    for (section in config().sections) {
      for ((key, value) in section.values) {
        repoConfig.setString(section.name, null, key, value)
      }
    }
  }

  override fun isStagingAreaDirty(): Boolean {
    val status = jgit.status().call()
    return !status.isClean
  }

  override fun checkout(branch: String, create: Boolean) {
    if (currentBranch().name == branch) return

    jgit.checkout().setName(branch)
        .setCreateBranch(create)
        .setUpstreamMode(SET_UPSTREAM)
        .call()
  }

  @Suppress("NAME_SHADOWING")
  override fun commitAll(
    message: String,
    author: GitAuthor?,
    timestamp: UtcTimestamp?,
    allowEmpty: Boolean
  ) {
    val author = timestamp?.let {
      val author = author ?: defaultCommitAuthor()
      PersonIdent(author.name, author.email, Date(it.millis), TimeZone.getTimeZone("UTC"))
    }

    if (headCommit() == null) {
      val message = """
          |JGit bug workaround
          |
          |Press uses JGit for syncing notes on Android, but it has an annoying
          |bug where commits without a parent get dropped after a rebase, which 
          |is always going to be true for the first commit. As a workaround, 
          |Press adds a dummy commit that sacrifices itself to protect an actual
          |commit. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=563805.
          """.trimMargin()
      jgit.commit()
          .setAllowEmpty(true)
          .setMessage(message)
          .setAuthor(author)
          .call()
    }

    jgit.add().addFilepattern(".").call()

    jgit.commit()
        // Calling 'add .' in JGit doesn't add deleted files, unlike
        // native git. So it's important to use setAll(true) here.
        .setAll(true)
        .setAllowEmpty(allowEmpty)
        .setMessage(message)
        .setAuthor(author)
        .call()
  }

  private fun defaultCommitAuthor(): GitAuthor {
    val config = jgit.repository.config.get(UserConfig.KEY)
    return GitAuthor(name = config.authorName, email = config.authorEmail)
  }

  override fun fetch() {
    jgit.fetch()
        .setTransportConfigCallback(sshTransport())
        .call()
  }

  override fun mergeConflicts(with: GitCommit): List<MergeConflict> {
    val head = headCommit() ?: return emptyList()

    val merger = ThreeWayMergeStrategy.RECURSIVE.newMerger(jgit.repository, true /* in-memory */)
    val canMerge = merger.merge(head.commit, with.commit)
    if (canMerge) {
      return emptyList()
    }

    check(merger is ResolveMerger)
    check(merger.unmergedPaths.isNotEmpty()) {
      "Merge will fail despite having zero conflicts. Failing paths: ${merger.failingPaths}"
    }
    return merger.unmergedPaths.map { path ->
      MergeConflict(path, theirContent = {
        val content = readFile(path, with)
        if (content != null) TheirContent.Modified(content)
        else TheirContent.Deleted
      })
    }
  }

  override fun rebase(with: GitCommit, strategy: MergeStrategy): RebaseResult {
    val rebaseResult = jgit.rebase()
        .setUpstream(with.commit)
        .setStrategy(strategy.toJgit())
        .call()

    val onAbort = {
      jgit.rebase().setOperation(ABORT).call()
      Unit
    }

    return with(rebaseResult) {
      when {
        status.isSuccessful -> RebaseResult.Success
        status == STOPPED -> RebaseResult.Failure(
            details = "Merge conflicts",
            abort = onAbort
        )
        else -> RebaseResult.Failure(
            details = "Unknown. Failing: $failingPaths, uncommitted: $uncommittedChanges",
            abort = onAbort
        )
      }
    }
  }

  override fun pull(rebase: Boolean): PullResult {
    val pullResult = jgit.pull()
        .apply {
          if (rebase) setRebase(REBASE)
          else setFastForward(FF)
        }
        .setStrategy(OURS.toJgit())
        .setTransportConfigCallback(sshTransport())
        .call()

    val onAbort = {
      Unit
    }

    return when {
      pullResult.isSuccessful -> PullResult.Success
      else -> PullResult.Failure(reason = pullResult.toString(), abort = onAbort)
    }
  }

  override fun merge(with: GitCommit): PullResult {
    val mergeResult = jgit.merge()
        .include(with.commit)
        .setFastForward(FF)
        //.setStrategy(org.eclipse.jgit.merge.MergeStrategy.THEIRS)
        //.setCommit(false)
        .call()

    val onAbort = {
      // https://stackoverflow.com/a/29815444/2511884
      jgit.repository.writeMergeCommitMsg(null)
      jgit.repository.writeMergeHeads(null)
      jgit.reset().setMode(ResetType.HARD).call()
      Unit
    }

    println("\nConflicting files:")
    for (conflictPath in mergeResult.conflicts?.keys ?: emptySet<String>()) {
      val content = readFile(conflictPath, with)
      println("$conflictPath -> ${content?.replace("\n", "\\n")}")
    }

    return when {
      mergeResult.mergeStatus.isSuccessful -> PullResult.Success
      else -> PullResult.Failure(reason = mergeResult.toString(), abort = onAbort)
    }
  }

  // TODO: return File directly.
  private fun readFile(path: String, inCommit: GitCommit): String? {
    val repository = jgit.repository
    var fileContent: String? = null

    RevWalk(repository).use { revWalk ->
      val tree = revWalk.parseCommit(inCommit.commit).tree

      TreeWalk(repository).use { treeWalk ->
        treeWalk.addTree(tree)
        treeWalk.isRecursive = true
        treeWalk.filter = PathFilter.create(path)
        if (treeWalk.next()) {
          val objectId: ObjectId = treeWalk.getObjectId(0)
          val loader: ObjectLoader = repository.open(objectId)
          fileContent = String(loader.bytes)
        }
      }
      revWalk.dispose()
    }

    return fileContent
  }

  override fun push(force: Boolean): PushResult {
    val pushResult = jgit.push()
        .setTransportConfigCallback(sshTransport())
        .setForce(force)
        .call()
        .toList()
        .also { check(it.size == 1) { "Did not expect multiple push results: $it" } }
        .single()

    return when (val status = pushResult.remoteUpdates.single().status) {
      RemoteRefUpdate.Status.OK -> PushResult.Success
      RemoteRefUpdate.Status.UP_TO_DATE -> PushResult.AlreadyUpToDate
      else -> PushResult.Failure(status.toString())
    }
  }

  private fun sshTransport(): TransportConfigCallback {
    return TransportConfigCallback { transport ->
      if (transport !is SshTransport) return@TransportConfigCallback

      transport.sshSessionFactory = object : JschConfigSessionFactory() {
        override fun configure(host: Host, session: Session) {
          session.setConfig("StrictHostKeyChecking", "no")
        }

        override fun createSession(hc: Host?, user: String?, host: String?, port: Int, fs: FS?): Session {
          // JSCH picks up SSH keys from ~/.ssh/config
          // which isn't needed and can potentially fail.
          val emptyHost = Host()
          return super.createSession(emptyHost, user, host, port, fs)
        }

        override fun createDefaultJSch(fs: FS): JSch {
          return super.createDefaultJSch(fs).apply {
            addIdentity(
                "deploy_key" /* name */,
                sshKey.key.toByteArray(),
                null        /* public key */,
                null        /* passphrase */
            )
          }
        }
      }
    }
  }

  override fun addRemote(name: String, url: String) {
    val existingRemote = jgit.remoteList().call()
        .map { it.name to it.urIs.single().toString() }
        .singleOrNull()

    if (existingRemote == name to url) return
    else if (existingRemote != null) error("Multiple remotes aren't supported")

    jgit.remoteAdd()
        .setName(name)
        .setUri(URIish(url))
        .call()
  }

  override fun headCommit(onBranch: String?): GitCommit? {
    val revision = onBranch ?: "HEAD"
    val head = jgit.repository.resolve(revision) ?: return null

    RevWalk(jgit.repository).use { walk ->
      val commit = walk.parseCommit(head)
      walk.dispose()
      return GitCommit(commit)
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  override fun commitsBetween(from: GitCommit?, toInclusive: GitCommit): List<GitCommit> {
    RevWalk(jgit.repository).use { walk ->
      walk.markStart(toInclusive.commit)

      val commits: List<GitCommit> = buildList {
        for (commit in walk) {
          add(GitCommit(commit))
          if (from != null && commit.id == from.commit.id) {
            break
          }
        }
      }
      walk.dispose()

      if (from != null && from.sha1 != commits.last().sha1) {
        // [from] isn't an ancestor of [toInclusive].
        val log = jgit.log().call().map { GitCommit(it) }
        error("Commits (${from.sha1} and ${toInclusive.sha1}) aren't in the same branch. Git log: $log")
      }

      return commits.reversed()
    }
  }

  override fun commonAncestor(first: GitCommit, second: GitCommit): GitCommit? {
    RevWalk(jgit.repository).use { walk ->
      walk.revFilter = RevFilter.MERGE_BASE
      walk.markStart(walk.parseCommit(first.commit))
      walk.markStart(walk.parseCommit(second.commit))
      val mergeBase: RevCommit? = walk.next()
      walk.dispose()
      return mergeBase?.let(::GitCommit)
    }
  }

  override fun changesIn(commit: GitCommit): GitTreeDiff {
    var parent: RevCommit? = null
    RevWalk(jgit.repository).use { walk ->
      // Not sure if picking the first parent is right if
      // multiple parents are present (in case of a merge).
      parent = walk.parseCommit(commit.commit.id).parents.firstOrNull()
      parent?.let { walk.parseHeaders(it) }
      walk.dispose()
    }
    return diffBetween(parent?.let(::GitCommit), commit)
  }

  // todo: rename to changesBetween()
  override fun diffBetween(from: GitCommit?, to: GitCommit): GitTreeDiff {
    val fromTree = from?.commit?.tree
    val toTree = to.commit.tree

    jgit.repository.newObjectReader().use { reader ->
      val firstTreeParser = when {
        fromTree != null -> CanonicalTreeParser().apply { reset(reader, fromTree.id) }
        else -> EmptyTreeIterator()
      }
      val secondTreeParser = CanonicalTreeParser().apply { reset(reader, toTree.id) }

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

  override fun currentBranch(): GitBranch {
    val fullBranch: String = jgit.repository.fullBranch ?: error("Repository is corrupt and has no HEAD")
    return if (fullBranch.startsWith("refs/heads/")) {
      GitBranch(name = JRepository.shortenRefName(fullBranch))
    } else {
      error("HEAD is detached and isn't pointing to any branch.")
    }
  }

  private fun printLog(title: String) {
    println(title)
    for (log in jgit.log().call()) {
      val relativeTime = Duration.ofMillis(System.currentTimeMillis() - log.authorIdent.`when`.time)
      println("${log.name.take(7)} - ${log.shortMessage} (${relativeTime.toHours()}h ago)")
    }
  }
}

private fun MergeStrategy.toJgit(): JgitMergeStrategy {
  return when (this) {
    OURS -> FakeOneSidedStrategy()
  }
}
