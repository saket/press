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
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.sync.Syncer
import me.saket.press.shared.time.Clock

// TODO: commit deleted and archived notes as well.
// TODO: commit only un-synced notes.
// TODO: push only if something was committed or pulled.
// TODO: add logging.
class GitSyncer(
  private val git: GitRepository,
  private val database: PressDatabase,
  private val deviceInfo: DeviceInfo,
  private val clock: Clock,
  private val deviceId: Setting<DeviceId>
) : Syncer {

  private val noteQueries get() = database.noteQueries
  private val directory = File(git.directoryPath)
  private val remoteSet = AtomicReference(false)
  private val register = fileRegister()

  // TODO: figure out this name and email.
  private val gitAuthor = GitAuthor("Saket", "pressapp@saket.me")

  override fun sync() {
    val reader = register.read()
    commitAllChanges(reader)
    pull(reader)
    push()
  }

  private fun fileRegister(): FileNameRegister =
    with(File(directory, ".press/register/")) {
      return FileNameRegister(this, deviceId.get())
    }

  private fun commitAllChanges(register: FileNameRegister.Reader) {
    ensureInitialCommit()
    directory.makeDirectory()

    // todo: add a column for tracking sync state.
    val unSyncedNotes = noteQueries.notes().executeAsList()
    if (unSyncedNotes.isEmpty()) {
      return
    }

    for (note in unSyncedNotes) {
      val noteFile = File(directory, register.fileNameFor(note))
      noteFile.write(note.content)
      register.save()

      git.addAll()
      git.commit(
          message = "Update '${noteFile.name}'",
          author = gitAuthor,
          timestamp = UtcTimestamp(note.updatedAt)
      )
    }
  }

  /**
   * JGit has a bug where rebasing a branch with a single commit ends up drops the commit.
   * Ensuring that local master has atleast one commit works around the bug. It's probably
   * also nice to have an initial commit marking the start of syncing on this device.
   *
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=563805
   */
  private fun ensureInitialCommit() {
    val head = git.headCommit()
    if (head != null) return

    check(git.currentBranch().name == "master")
    git.commit(
        message = "Setup syncing on ${deviceInfo.deviceName()}",
        author = gitAuthor,
        timestamp = UtcTimestamp(clock.nowUtc()),
        allowEmpty = true
    )
  }

  private fun pull(register: FileNameRegister.Reader) {
    require(remoteSet.get())

    git.fetch()
    val localHead = git.headCommit()!!  // non-null because of ensureInitialCommit().
    val upstreamHead = git.headCommit(onBranch = "origin/master")

    if (localHead == upstreamHead || upstreamHead == null) {
      // Nothing to fetch.
      return
    }

    val rebaseResult = git.rebase(with = upstreamHead)
    require(rebaseResult !is RebaseResult.Failure) { "Failed to rebase: $rebaseResult" }

    // A rebase will cause the history to be re-written, so we need
    // to find the first common ancestor of local and upstream. All
    // commits from the ancestor to the current HEAD will have to be
    // (re)processed.
    val commitsUpdated = git.commitsBetween(
        from = git.commonAncestor(localHead, upstreamHead),
        toInclusive = git.headCommit()!!
    )

    // It would be easier to diff between the common ancestor and the
    // current HEAD, but Press stores meta information of notes (e.g.,
    // updated-at timestamp) in each commit so they need to be processed
    // one-by-one.
    val commitsToDiff = commitsUpdated.mapIndexed { index, commit ->
      commit to git.diffBetween(from = commitsUpdated.getOrNull(index - 1), to = commit)
    }

    val dbOperations = mutableListOf<Runnable>()

    for ((commit, diffs) in commitsToDiff) {
      for (diff in diffs.filter { it.path.endsWith(".md") }) {
        dbOperations += when (diff) {
          is Add -> {
            val content = File(directory, diff.path).read()
            val existingId = register.noteIdFor(diff.path)
            val createdAt = DateTime.fromUnix(commit.utcTimestamp.millis)
            Runnable {
              if (existingId != null) {
                noteQueries.updateContent(
                    uuid = NoteId.generate(),
                    content = content,
                    updatedAt = createdAt
                )
              } else {
                noteQueries.insert(
                    uuid = NoteId.generate(),
                    content = content,
                    createdAt = createdAt,
                    updatedAt = createdAt
                )
              }
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
fun UtcTimestamp(time: DateTime): UtcTimestamp {
  return UtcTimestamp(time.unixMillisLong)
}
