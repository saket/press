package me.saket.press.shared.sync.git

import co.touchlab.stately.concurrency.AtomicReference
import com.soywiz.klock.DateTime
import kotlinx.coroutines.Runnable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration.Companion.Stable
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
import me.saket.press.shared.sync.git.GitSyncer.Result.DONE
import me.saket.press.shared.sync.git.GitSyncer.Result.SKIPPED
import me.saket.press.shared.time.Clock

// TODO: commit deleted and archived notes as well.
// TODO: commit only un-synced notes.
// TODO: add logging.
// TODO: figure out git author name/email.
class GitSyncer(
  private val git: GitRepository,
  private val database: PressDatabase,
  private val deviceInfo: DeviceInfo,
  private val clock: Clock
) : Syncer {

  private val noteQueries get() = database.noteQueries
  private val directory = File(git.directoryPath)
  private val register = FileNameRegister(Json(Stable.copy(prettyPrint = true)))
  private val remoteSet = AtomicReference(false)
  private val gitAuthor = GitAuthor("Saket", "pressapp@saket.me")

  private enum class Result {
    DONE,
    SKIPPED
  }

  init {
    git.workaroundJgitBug = true
  }

  override fun sync() {
    val reader = register.read(directory, deviceId)
    val commitResult = commitAllChanges(reader)
    val pullResult = pull(reader)

    if (commitResult == DONE || pullResult == DONE) {
      push()
    }
  }

  private fun commitAllChanges(register: FileNameRegister.Reader): Result {
    maybeMakeInitialCommit()
    directory.makeDirectory()

    val unSyncedNotes = noteQueries.notes().executeAsList()
    if (unSyncedNotes.isEmpty()) {
      return SKIPPED
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
    return DONE
  }

  /** Announcement commit when syncing begins. */
  private fun maybeMakeInitialCommit() {
    check(git.currentBranch().name == "master")
    val head = git.headCommit()
    if (head != null) return

    // Empty commits get thrown away on a rebase so gotta add something -_-.
    with(File(directory, ".press/")) {
      makeDirectory(recursively = true)
      File(this, "README.md").write(
          "Press uses files in this directory for storing meta-data of your synced notes. " +
              "They are auto-generated and shouldn't be modified. If you run into any " +
              "issues with syncing of notes, feel free to file a [bug report here]" +
              "(https://github.com/saket/press/issues)."
      )
    }

    git.addAll()
    git.commit(
        message = "Setup syncing on ${deviceInfo.deviceName()}",
        author = gitAuthor,
        timestamp = UtcTimestamp(clock.nowUtc()),
        allowEmpty = true
    )
  }

  private fun pull(register: FileNameRegister.Reader): Result {
    require(remoteSet.get())

    git.fetch()
    val localHead = git.headCommit()!!  // non-null because of ensureInitialCommit().
    val upstreamHead = git.headCommit(onBranch = "origin/master")

    if (localHead == upstreamHead || upstreamHead == null) {
      // Nothing to fetch.
      return SKIPPED
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
      for (diff in diffs) {
        if (!diff.path.endsWith(".md")) {
          // Not a note, ignore.
          continue
        }
        if (diff.path.contains("/")) {
          // Nested notes aren't supported yet.
          continue
        }

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
    return DONE
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
