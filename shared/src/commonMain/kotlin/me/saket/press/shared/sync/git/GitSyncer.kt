package me.saket.press.shared.sync.git

import com.soywiz.klock.DateTime
import kotlinx.coroutines.Runnable
import me.saket.kgit.Git
import me.saket.kgit.GitAuthor
import me.saket.kgit.GitCommit
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
import me.saket.press.shared.settings.Setting
import me.saket.press.shared.sync.SyncState.IN_FLIGHT
import me.saket.press.shared.sync.SyncState.SYNCED
import me.saket.press.shared.sync.Syncer
import me.saket.press.shared.sync.git.GitSyncer.Result.DONE
import me.saket.press.shared.sync.git.GitSyncer.Result.SKIPPED
import me.saket.press.shared.time.Clock
import me.saket.wysiwyg.atomicLazy

// TODO:
//  Stop ship
//   - read default branch name from the repository instead of hardcoding to 'master.
//   - handle all GitTreeDiff.Change types.
//   - broadcast an event when a merge conflict is resolved.
//  Others
//   - figure out git author name/email.
//   - set both author and committer time.
//   - add logging.
//   - commit deleted notes.
class GitSyncer(
  git: Git,
  config: Setting<GitSyncerConfig>,
  private val directory: File,
  private val database: PressDatabase,
  private val deviceInfo: DeviceInfo,
  private val clock: Clock
) : Syncer {

  private val noteQueries get() = database.noteQueries
  private val register = FileNameRegister(directory)
  private val gitAuthor = GitAuthor("Saket", "pressapp@saket.me")

  // Lazy to avoid reading anything on the main thread.
  private val git by atomicLazy {
    with(config.get()!!) {
      git.repository(sshKey = sshKey, path = directory.path).apply {
        addRemote("origin", remote.sshUrl)
      }
    }
  }

  private enum class Result {
    DONE,
    SKIPPED
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
    val pendingSyncNotes = noteQueries.pendingSyncNotes().executeAsList()
    if (pendingSyncNotes.isEmpty()) {
      return SKIPPED
    }

    // Having an intermediate sync state between PENDING and SYNCED
    // is important in case a note gets updated while it is syncing,
    // in which case it'll get marked as PENDING again.
    noteQueries.updateSyncState(
        ids = pendingSyncNotes.map { it.id },
        syncState = IN_FLIGHT
    )

    for (note in pendingSyncNotes) {
      val noteFile = register.fileFor(note)
      noteFile.write(note.content)

      // changes when the same notes are written to files.
      if (git.isStagingAreaDirty()) {
        git.commitAll(
            message = "Update '${noteFile.name}'",
            author = gitAuthor,
            timestamp = UtcTimestamp(note.updatedAt)
        )
      }
    }
    return DONE
  }

  /** Commit announcing that syncing has been setup. */
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
    // changes from the ancestor to the current HEAD will have to be
    // (re)processed.
    processNotesFromCommits(
        from = git.commonAncestor(localHead, upstreamHead),
        to = git.headCommit()!!
    )
    return DONE
  }

  private fun processNotesFromCommits(from: GitCommit?, to: GitCommit) {
    // Press stores updated-at timestamp of notes in each commit.
    val diffPathTimestamps = git.commitsBetween(from = from, toInclusive = to)
        .flatMap { commit ->
          git.changesIn(commit)
              .filterNoteChanges()
              .map { it.path to commit.dateTime }
        }
        .toMap()

    // DB operations are executed in one go to
    // avoid locking the DB in a transaction for long.
    val dbOperations = mutableListOf<Runnable>()

    println("\n-------------------------")

    for (diff in git.diffBetween(from, to).filterNoteChanges()) {
      println("$diff")
      val commitTime = diffPathTimestamps[diff.path]!!

      dbOperations += when (diff) {
        is Rename,
          // Renaming of note files are ignored. Press
          // generates a name as per the note's heading.
        is Add, is Modify -> {
          val file = File(directory, diff.path)
          val content = file.read()

          val oldPath = if (diff is Rename) diff.fromPath else null
          val record = register.recordFor(diff.path, oldPath = oldPath)
          val existingId = record?.noteId
          val isArchived = record?.noteFolder == "archived"

          if (existingId != null) {
            println("Updating $existingId (${diff.path}), isArchived? $isArchived")
            Runnable {
              noteQueries.updateContent(
                  id = existingId,
                  content = content,
                  updatedAt = commitTime
              )
              noteQueries.setArchived(
                  id = existingId,
                  isArchived = isArchived,
                  updatedAt = commitTime
              )
            }
          } else {
            val newId = NoteId.generate()
            println("Creating new note $newId for (${diff.path}), isArchived? $isArchived")
            register.createNewRecordFor(file, newId)
            Runnable {
              noteQueries.insert(
                  id = newId,
                  content = content,
                  createdAt = commitTime,
                  updatedAt = commitTime
              )
              noteQueries.setArchived(
                  id = newId,
                  isArchived = isArchived,
                  updatedAt = commitTime
              )
            }
          }
        }
        is Delete -> {
          val noteId = register.noteIdFor(diff.path)
          requireNotNull(noteId) { "Deleting non-existent note: $noteId" }
          println("Permanently deleting $noteId (${diff.path})")
          Runnable {
            noteQueries.markAsPendingDeletion(noteId)
            noteQueries.deleteNote(noteId)
          }
        }
        is Copy -> TODO("handle copy of ${diff.fromPath} -> ${diff.toPath}")
      }
    }

    if (dbOperations.isNotEmpty()) {
      noteQueries.transaction {
        dbOperations.forEach { it.run() }
      }
    }

    val savedNotes = noteQueries.allNotes().executeAsList()
    register.pruneStaleRecords(savedNotes)
    if (git.isStagingAreaDirty()) {
      git.commitAll(
          message = "Update file name records",
          author = gitAuthor,
          timestamp = UtcTimestamp(clock)
      )
    }
  }

  private fun List<GitTreeDiff.Change>.filterNoteChanges() = filter { diff ->
    val path = diff.path
    when {
      !path.endsWith(".md") -> false                                        // Not a markdown note.
      path.startsWith(".press/") -> false                                   // Meta-files, ignore.
      path.contains("/") -> {
        when {
          path.startsWith("archived/") && !path.hasMultipleOf('/') -> true  // Archived note.
          else -> error("Folders aren't supported yet: '$path'")
        }
      }
      else -> true
    }
  }

  private fun push() {
    val pushResult = git.push()
    require(pushResult !is PushResult.Failure) { "Failed to push: $pushResult" }

    noteQueries.swapSyncStates(old = IN_FLIGHT, new = SYNCED)
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

private val GitCommit.dateTime: DateTime
  get() = DateTime.fromUnix(utcTimestamp.millis)
