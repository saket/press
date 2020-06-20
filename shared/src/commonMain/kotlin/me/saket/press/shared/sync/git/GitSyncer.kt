package me.saket.press.shared.sync.git

import co.touchlab.stately.concurrency.AtomicReference
import com.soywiz.klock.DateTime
import kotlinx.coroutines.Runnable
import me.saket.kgit.GitAuthor
import me.saket.kgit.GitCommit
import me.saket.kgit.GitRepository
import me.saket.kgit.GitTreeDiff
import me.saket.kgit.GitTreeDiff.Change.Add
import me.saket.kgit.GitTreeDiff.Change.Copy
import me.saket.kgit.GitTreeDiff.Change.Delete
import me.saket.kgit.GitTreeDiff.Change.Modify
import me.saket.kgit.GitTreeDiff.Change.Rename
import me.saket.kgit.MergeStrategy.OURS
import me.saket.kgit.PushResult
import me.saket.kgit.RebaseResult
import me.saket.kgit.UtcTimestamp
import me.saket.press.PressDatabase
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.sync.Syncer
import me.saket.press.shared.sync.git.GitSyncer.Result.DONE
import me.saket.press.shared.sync.git.GitSyncer.Result.SKIPPED
import me.saket.press.shared.time.Clock

// TODO: commit deleted and archived notes as well.
// TODO: commit only un-synced notes.
// TODO: add logging.
// TODO: figure out git author name/email.
// TODO: handle all GitTreeDiff.Change types.
// TODO: Broadcast an event when a merge conflict is resolved.
class GitSyncer(
  private val git: GitRepository,
  private val database: PressDatabase,
  private val deviceInfo: DeviceInfo,
  private val clock: Clock
) : Syncer {

  private val noteQueries get() = database.noteQueries
  private val directory = File(git.directoryPath)
  private val register = FileNameRegister(directory)
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
    maybeMakeInitialCommit()
    directory.makeDirectory()

    val commitResult = commitAllChanges()
    val pullResult = pull()

    if (commitResult == DONE || pullResult == DONE) {
      push()
    }
  }

  private fun commitAllChanges(): Result {
    val unSyncedNotes = noteQueries.notes().executeAsList()
    if (unSyncedNotes.isEmpty()) {
      return SKIPPED
    }

    for (note in unSyncedNotes) {
      val noteFile = File(directory, register.fileNameFor(note))
      noteFile.write(note.content)

      git.commitAll(
          message = "Update '${noteFile.name}'",
          author = gitAuthor,
          timestamp = UtcTimestamp(note.updatedAt)
      )
    }
    return DONE
  }

  /** Commit announcing syncing has begun. */
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

    git.commitAll(
        message = "Setup syncing on ${deviceInfo.deviceName()}",
        author = gitAuthor,
        timestamp = UtcTimestamp(clock),
        allowEmpty = true
    )
  }

  private fun pull(): Result {
    require(remoteSet.get())

    git.fetch()
    val localHead = git.headCommit()!!  // non-null because of ensureInitialCommit().
    val upstreamHead = git.headCommit(onBranch = "origin/master")

    if (localHead == upstreamHead || upstreamHead == null) {
      // Nothing to fetch.
      return SKIPPED
    }

    // Git makes it easy to handle merge conflicts, but automating it for
    // the user is going to be a challenge. If the same note was modified
    // from different devices, Press duplicates them: note.md & note_2.md.
    // This will also cover non-.md files.
    //
    // It's _very_ important that the local copy is duplicated. It'd be
    // nice to rename the upstream copy because it's possible that the
    // local copy is being edited right now, but duplicating the remote
    // copy will result in an infinite loop where a new copy is created
    // on every sync on the other device.
    //
    // This file will get processed after rebase.
    val conflicts = git.mergeConflicts(with = upstreamHead)
    if (conflicts.isNotEmpty()) {
      for (conflict in conflicts) {
        val conflictingNote = File(directory, conflict.path)
        val newName = register.findNewNameOnConflict(conflictingNote)
        conflictingNote.copy(newName, recursively = true)
      }

      git.commitAll(
          message = "Auto-resolve merge conflicts",
          author = gitAuthor,
          timestamp = UtcTimestamp(clock)
      )
    }

    val rebaseResult = git.rebase(with = upstreamHead, strategy = OURS)
    require(rebaseResult !is RebaseResult.Failure) { "Failed to rebase: $rebaseResult" }

    // A rebase will cause the history to be re-written, so we need
    // to find the first common ancestor of local and upstream. All
    // commits from the ancestor to the current HEAD will have to be
    // (re)processed.
    //
    // It would be easier to diff between the common ancestor and the
    // current HEAD, but Press stores meta information of notes (e.g.,
    // updated-at timestamp) in each commit so they need to be processed
    // one-by-one.
    val updatedCommits = git.commitsBetween(
        from = git.commonAncestor(localHead, upstreamHead),
        toInclusive = git.headCommit()!!
    )
    reprocessNotesFromCommits(updatedCommits)
    return DONE
  }

  private fun reprocessNotesFromCommits(commits: List<GitCommit>) {
    val changes = commits.zipWithNext(initialValue = null).map { (prev, current) ->
      current to git.diffBetween(prev, current).filterNoteChanges()
    }

    val dbOperations = mutableListOf<Runnable>()

    for ((commit, diffs) in changes) {
      for (diff in diffs.filterNoteChanges()) {
        dbOperations += when (diff) {
          is Add, is Modify -> {
            val content = File(directory, diff.path).read()
            val existingId = register.noteIdFor(diff.path)
            val createdAt = DateTime.fromUnix(commit.utcTimestamp.millis)
            Runnable {
              if (existingId != null) {
                noteQueries.updateContent(
                    uuid = existingId,
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
          is Copy -> TODO("handle copy of ${diff.fromPath} -> ${diff.toPath}")
          is Delete -> TODO("handle deletion of ${diff.path}")
          is Rename -> TODO("handle rename of ${diff.fromPath} -> ${diff.toPath}")
        }
      }
    }

    if (dbOperations.isNotEmpty()) {
      noteQueries.transaction {
        dbOperations.forEach { it.run() }
      }
    }
  }

  private fun List<GitTreeDiff.Change>.filterNoteChanges() = filter { diff ->
    when {
      !diff.path.endsWith(".md") -> false       // Not a markdown note, at-least not managed by Press.
      diff.path.startsWith(".press/") -> false  // Meta-files, ignore.
      diff.path.contains("/") -> error("Nested notes aren't supported yet: '${diff.path}'")
      else -> true
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

@Suppress("FunctionName")
fun UtcTimestamp(clock: Clock): UtcTimestamp {
  return UtcTimestamp(clock.nowUtc())
}
