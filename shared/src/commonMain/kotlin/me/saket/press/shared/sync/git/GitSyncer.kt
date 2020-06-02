package me.saket.press.shared.sync.git

import co.touchlab.stately.concurrency.AtomicReference
import com.soywiz.klock.DateTime
import kotlinx.coroutines.Runnable
import me.saket.kgit.GitAuthor
import me.saket.kgit.GitRepository
import me.saket.kgit.GitTreeDiff.Change.Add
import me.saket.kgit.GitTreeDiff.Change.Copy
import me.saket.kgit.GitTreeDiff.Change.Delete
import me.saket.kgit.GitTreeDiff.Change.Modify
import me.saket.kgit.GitTreeDiff.Change.Rename
import me.saket.kgit.PushResult
import me.saket.kgit.RebaseResult
import me.saket.kgit.UtcTimestamp
import me.saket.press.PressDatabase
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.home.SplitHeadingAndBody
import me.saket.press.shared.sync.Syncer
import me.saket.press.shared.time.Clock

// TODO: commit deleted and archived notes as well.
// TODO: commit only un-synced notes.
// TODO: push only if something was committed or pulled.
class GitSyncer(
  private val git: GitRepository,
  private val database: PressDatabase,
  private val deviceInfo: DeviceInfo,
  private val clock: Clock
) : Syncer {

  private val noteQueries get() = database.noteQueries
  private val directory = File(git.directoryPath)
  private val remoteSet = AtomicReference(false)

  // TODO: figure out this name and email.
  private val gitAuthor = GitAuthor("Saket", "pressapp@saket.me")

  override fun sync() {
    require(remoteSet.get()) { "Remote isn't set" }
    commitAllChanges()
    pull()
    push()
  }

  private fun commitAllChanges() {
    ensureInitialCommit()

    val unSyncedNotes = noteQueries.notes().executeAsList()
    if (unSyncedNotes.isEmpty()) {
      return
    }

    directory.makeDirectory()
    for (note in unSyncedNotes) {
      val (heading) = SplitHeadingAndBody.split(note.content)
      check(heading.isNotBlank()) { "Heading is empty for: '${note.content}'" }
      val noteFileName = FileNameSanitizer.sanitize(heading, maxLength = 255)
      File(directory, "$noteFileName.md").write(note.content)

      git.addAll()
      git.commit(
          message = "Update '$heading'",
          author = gitAuthor,
          timestamp = UtcTimestamp(note.updatedAt)
      )
    }
  }

  /**
   * JGit has a bug where rebasing a branch with a single commit ends up drops the commit.
   * Ensuring that local master has atleast one commit works around the bug. It's probably
   * also nice to have an initial commit marking the start of syncing on this device.s
   */
  private fun ensureInitialCommit() {
    val head = git.headCommit()
    if (head == null) {
      check(git.currentBranch().name == "master")
      git.commit(
          message = "Setup syncing on ${deviceInfo.deviceName()}",
          author = gitAuthor,
          timestamp = UtcTimestamp(clock.nowUtc()),
          allowEmpty = true
      )
    }
  }

  private fun pull() {
    val headBeforePull = git.headCommit()

    git.fetch()
    git.checkout("origin/master")
    val originHead = git.headCommit()
    git.checkout("master")

    if (originHead != null) {
      val rebaseResult = git.rebase(with = originHead)
      require(rebaseResult !is RebaseResult.Failure) { "Failed to rebase: $rebaseResult" }
    }

    val headAfterPull = git.headCommit()

    if (headAfterPull == headBeforePull) {
      // No changes received.
      return
    }

    // HEAD after pull can't be null if it's not equal to the
    // HEAD before pull. The git history always moves forward.
    val commitsPulled = git.commitsBetween(from = headBeforePull, to = headAfterPull!!)
    val commitsToDiff = commitsPulled.mapIndexed { index, commit ->
      commit to git.diffBetween(from = commitsPulled.getOrNull(index - 1), to = commit)
    }

    val dbOperations = mutableListOf<Runnable>()

    for ((commit, diffs) in commitsToDiff) {
      for (diff in diffs) {
        dbOperations += when (diff) {
          is Add -> {
            val content = File(directory, diff.path).read()
            val createdAt = DateTime.fromUnix(commit.utcTimestamp.millis)
            Runnable {
              noteQueries.insert(
                  uuid = NoteId.generate(),
                  content = content,
                  createdAt = createdAt,
                  updatedAt = createdAt
              )
            }
          }
          is Modify -> TODO()
          is Copy -> TODO()
          is Delete -> TODO()
          is Rename -> TODO()
        }
      }
    }

    if (dbOperations.isNotEmpty()) {
      noteQueries.transaction {
        dbOperations.forEach { it.run() }
      }
    }
  }

  private fun push() {
    val pushResult = git.push()
    require(pushResult !is PushResult.Failure) { "Failed to push: $pushResult" }
  }

  fun setRemote(remoteUrl: String) {
    git.addRemote("origin", remoteUrl)
    remoteSet.set(true)
  }
}

@Suppress("FunctionName")
private fun UtcTimestamp(time: DateTime): UtcTimestamp {
  return UtcTimestamp(time.unixMillisLong)
}
