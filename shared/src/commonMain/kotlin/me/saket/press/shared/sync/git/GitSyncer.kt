package me.saket.press.shared.sync.git

import co.touchlab.stately.concurrency.AtomicBoolean
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.combineLatest
import com.badoo.reaktive.scheduler.ioScheduler
import com.soywiz.klock.DateTime
import kotlinx.coroutines.Runnable
import me.saket.kgit.Git
import me.saket.kgit.GitCommit
import me.saket.kgit.GitConfig
import me.saket.kgit.GitError.AuthFailed
import me.saket.kgit.GitError.NetworkError
import me.saket.kgit.GitError.Unknown
import me.saket.kgit.GitPullResult
import me.saket.kgit.GitRepository
import me.saket.kgit.GitTreeDiff
import me.saket.kgit.GitTreeDiff.Change.Add
import me.saket.kgit.GitTreeDiff.Change.Copy
import me.saket.kgit.GitTreeDiff.Change.Delete
import me.saket.kgit.GitTreeDiff.Change.Modify
import me.saket.kgit.GitTreeDiff.Change.Rename
import me.saket.kgit.PushResult.Failure
import me.saket.kgit.UtcTimestamp
import me.saket.kgit.abbreviated
import me.saket.kgit.identify
import me.saket.press.PressDatabase
import me.saket.press.data.shared.FolderSyncConfig
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.note.HeadingAndBody
import me.saket.press.shared.note.NoteFolder
import me.saket.press.shared.rx.asObservable
import me.saket.press.shared.rx.mapToOneOrOptional
import me.saket.press.shared.sync.LastSyncedAt
import me.saket.press.shared.sync.SyncMergeConflicts
import me.saket.press.shared.sync.SyncState
import me.saket.press.shared.sync.SyncState.IN_FLIGHT
import me.saket.press.shared.sync.SyncState.PENDING
import me.saket.press.shared.sync.SyncState.SYNCED
import me.saket.press.shared.sync.Syncer
import me.saket.press.shared.sync.Syncer.Status.LastOp.Failed
import me.saket.press.shared.sync.Syncer.Status.LastOp.Idle
import me.saket.press.shared.sync.Syncer.Status.LastOp.InFlight
import me.saket.press.shared.sync.git.FileNameRegister.FileSuggestion
import me.saket.press.shared.time.Clock

// TODO: show errors in status UI
class GitSyncer(
  git: Git,
  private val folder: NoteFolder?,
  private val database: PressDatabase,
  private val deviceInfo: DeviceInfo,
  private val clock: Clock,
  private val strings: Strings,
  private val mergeConflicts: SyncMergeConflicts,
  private val backupBeforeFirstSync: AtomicBoolean = AtomicBoolean(true)
) : Syncer() {
  internal val directory = File(deviceInfo.appStorage, "git")
  private val noteQueries get() = database.noteQueries
  private val configQueries get() = database.folderSyncConfigQueries
  private val register = FileNameRegister(directory)
  private val loggers = SyncLoggers(PrintLnSyncLogger, FileBasedSyncLogger(directory))
  private val git = {
    val remote = readConfig()!!.remote
    git.repository(
        path = directory.path,
        sshKey = remote.sshKey,
        remoteSshUrl = remote.remote.sshUrl,
        userConfig = GitConfig(
            "author" to listOf("name" to remote.user.name, "email" to (remote.user.email ?: "")),
            "committer" to listOf("name" to "press", "email" to "press@saket.me"),
            "diff" to listOf("renames" to "true")
        )
    )
  }

  companion object {
    private val lastOp = ObservableMutableMap<NoteFolder?, Status.LastOp>()
  }

  override fun status(): Observable<Status> {
    val config = configQueries.select(folder)
        .asObservable(ioScheduler)
        .mapToOneOrOptional()

    return combineLatest(config, lastOp.listen()) { (config), ops ->
      when (config) {
        null -> Status.Disabled
        else -> Status.Enabled(
            lastOp = ops[folder] ?: Idle,
            lastSyncedAt = config.lastSyncedAt?.let(::LastSyncedAt),
            syncingWith = config.remote.remote
        )
      }
    }
  }

  override fun sync() {
    if (readConfig() == null) return        // Sync is disabled.
    if (lastOp[folder] == InFlight) return  // Another sync ongoing.

    lastOp[folder] = InFlight
    loggers.onSyncStart(fromDevice = deviceInfo.deviceName())
    directory.makeDirectory(recursively = true)

    try {
      with(SyncTransaction(git())) {
        resetState()
        backupIfFirstSync()

        val pullResult = pull()
        commit(pullResult)
        processCommits(pullResult)
        push(pullResult)
      }
      lastOp[folder] = Idle

    } catch (e: Throwable) {
      when (Git.identify(e)) {
        NetworkError -> log("Network error. Will retry later.")
        Unknown -> log("Unknown error. Will retry later. ${e.stackTraceToString()}")
        AuthFailed -> {
          log("Auth failed. Deploy key was likely revoked. Disabling sync. ${e.stackTraceToString()}")
          disable()
        }
      }.exhaustive

      lastOp[folder] = Failed
      loggers.onSyncComplete()
      mergeConflicts.clear()
    }
  }

  private class SyncTransaction(val git: GitRepository) {
    // DB operations are executed in one go to
    // avoid locking the DB in a transaction for long.
    val dbOperations = mutableListOf<Runnable>()
  }

  override fun disable() {
    log("Disabling sync.")
    configQueries.delete(folder)
    directory.delete(recursively = true)
    noteQueries.swapSyncStates(old = SyncState.values().toList(), new = PENDING)
  }

  private fun SyncTransaction.resetState() {
    // Commit an announcement that syncing has been setup.
    if (git.headCommit() == null) {
      with(File(directory, ".press/")) {
        makeDirectory(recursively = true)
        File(this, "README.md").write(
            "Press uses files in this directory for storing meta-data of your synced notes. " +
                "They are auto-generated and shouldn't be modified. If you run into any " +
                "issues with syncing of notes, feel free to file a [bug report here]" +
                "(https://github.com/saket/press/issues) and attach [sync logs](sync_log.txt)" +
                " after removing/redacting any private info."
        )
      }

      git.commitAll(
          message = "Setup syncing on '${deviceInfo.deviceName()}'",
          timestamp = UtcTimestamp(clock),
          allowEmpty = true
      )
    }

    // Remove all unsynced and dirty changes.
    val config = readConfig()!!
    val lastCleanSha1 = config.lastPushedSha1 ?: git.headCommit()!!.sha1.value
    log("Resetting to sha1: $lastCleanSha1.")
    git.hardResetTo(
        sha1 = lastCleanSha1,
        resetState = true,
        deleteUntrackedFiles = true
    )

    // JGit doesn't offer a way to set the initial branch name and it
    // won't allow changing the branch without committing anything either
    // so Press changes it after committing something. This also acts as a
    // rollback if git is stuck in a detached head or something.
    git.checkout(config.remote.remote.defaultBranch, createIfNeeded = true)

    check(!git.isStagingAreaDirty()) { "Hard reset didn't work" }
  }

  /**
   * Press uses [TimeBasedConflictResolver] for the first sync which can potentially
   * accidentally delete users' notes. As a backup, user's notes are saved in a separate
   * branch so that they can be manually recovered.
   */
  private fun SyncTransaction.backupIfFirstSync() {
    if (!backupBeforeFirstSync.value) return
    if (readConfig()!!.lastPushedSha1 != null) return

    val localNotes = noteQueries.allNotes().executeAsList()
    if (localNotes.isEmpty()) {
      return
    }

    val backupBranch = "notes-backup-${clock.nowUtc().unixMillisLong}"
    log("Syncing for the first time. Backing up notes to '$backupBranch' branch.")

    val restoreToBranch = git.currentBranch()
    git.checkout(backupBranch)

    for (note in localNotes) {
      register.suggestFile(note).suggestedFile.write(note.content)
    }
    git.commitAll(
        message = """
          |Backup notes on '${deviceInfo.deviceName()}'
          |
          |This is a copy of your notes before Press started syncing them. 
          |In case something goes wrong with the first sync, your notes 
          |can be recovered from this commit.
          """.trimMargin(),
        timestamp = UtcTimestamp(clock),
        allowEmpty = true
    )
    git.push(force = true)

    git.checkout(restoreToBranch.name)
  }

  private data class PullResult(
    val headBefore: GitCommit,
    val headAfter: GitCommit
  )

  private fun SyncTransaction.pull(): PullResult {
    val localHead = git.headCommit()!!  // non-null because of resetState().

    git.pull(rebase = true).also {
      check(it is GitPullResult.Success) { "Failed to rebase: $it" }
    }

    val upstreamHead = git.headCommit()!!
    if (upstreamHead != localHead) {
      log("Pulled upstream. Moved head from $localHead to $upstreamHead.")
      val diff = git.changesBetween(localHead, upstreamHead)
      if (diff.isNotEmpty()) {
        log("\nPulled changes (${diff.size}):")
        log(diff.flattenToString())
      }
    } else {
      log("\nNothing to pull.")
    }

    // This ideally shouldn't be needed, but bugs have crept up in the
    // past and invalid records will need to be thrown away. Notes with
    // discarded records will be given a new identity.
    register.pruneDuplicateRecords()
    if (git.isStagingAreaDirty()) {
      log("\nPruned invalid file name records")
      git.commitAll(
          message = "Prune invalid file name records",
          timestamp = UtcTimestamp(clock)
      )
    }

    return PullResult(headBefore = localHead, headAfter = upstreamHead)
  }

  @Suppress("CascadeIf")
  private fun SyncTransaction.commit(pullResult: PullResult) {
    val pendingSyncNotes = noteQueries.notesInState(listOf(PENDING, IN_FLIGHT)).executeAsList()
    if (pendingSyncNotes.isEmpty()) {
      log("\nNothing to commit.")
      return
    }

    // Having an intermediate sync state between PENDING and SYNCED
    // is important in case a note gets updated while it is syncing,
    // in which case it'll get marked as PENDING again.
    noteQueries.updateSyncState(
        ids = pendingSyncNotes.map { it.id },
        syncState = IN_FLIGHT
    )

    // When syncing notes for the first time, pick newer notes.
    // If syncing was enabled sometime in the past, the local and the
    // remote repositories may have moved apart a lot.
    val isFirstSync = readConfig()!!.lastPushedSha1 == null
    val conflictResolver = when {
      isFirstSync -> TimeBasedConflictResolver(git, pullResult)
      else -> DuplicateOnConflictResolver(git, pullResult)
    }

    log("\nReading unsynced notes (${pendingSyncNotes.size}):")

    for (note in pendingSyncNotes) {
      val suggestion = register.suggestFile(note)
      val notePath = suggestion.suggestedFilePath
      log(" • $notePath (old=${suggestion.oldFilePath})")

      conflictResolver.resolveAndSave(note, suggestion, commitRename = {
        git.commitAll(
            message = "Rename '${suggestion.oldFilePath}' → '$notePath'",
            timestamp = UtcTimestamp(note.updatedAt),
            allowEmpty = false
        )
      })

      // Staging area may not be dirty if this note had already been processed earlier.
      if (git.isStagingAreaDirty()) {
        git.commitAll(
            message = "Update '$notePath'",
            timestamp = UtcTimestamp(note.updatedAt)
        )
      }

      if (note.isPendingDeletion && suggestion.suggestedFile.exists) {
        suggestion.suggestedFile.delete()

        git.commitAll(
            message = "Delete '$notePath'",
            timestamp = UtcTimestamp(note.updatedAt)
        )
      }
    }
  }

  private abstract class MergeConflictsResolver(git: GitRepository, pullResult: PullResult) {
    protected val pulledPaths = git
        .changesBetween(pullResult.headBefore, pullResult.headAfter)
        .filterNoteChanges()
        .map { it.path }
        .toHashSet()

    abstract fun resolveAndSave(note: Note, suggestion: FileSuggestion, commitRename: () -> Unit?)
  }

  private inner class TimeBasedConflictResolver(
    git: GitRepository,
    pullResult: PullResult
  ) : MergeConflictsResolver(git, pullResult) {
    private val pulledPathTimestamps = git
        .commitsBetween(null, pullResult.headAfter)
        .pathTimestamps(git)

    @Suppress("MapGetWithNotNullAssertionOperator")
    override fun resolveAndSave(note: Note, suggestion: FileSuggestion, commitRename: () -> Unit?) {
      val noteFile = suggestion.suggestedFile
      val notePath = suggestion.suggestedFilePath
      val oldPath = suggestion.oldFilePath

      val isRemoteNewer = { path: String ->
        path in pulledPaths && pulledPathTimestamps[path]!! > note.updatedAt
      }

      if (isRemoteNewer(notePath) || (oldPath != null && isRemoteNewer(oldPath))) {
        log("   picking remote's copy and discarding local")

      } else {
        suggestion.acceptRename?.let {
          it.invoke()
          commitRename()
        }
        log("   picking local copy and discarding remote (if any)")
        noteFile.write(note.content)
      }
    }
  }

  private inner class DuplicateOnConflictResolver(
    git: GitRepository,
    pullResult: PullResult
  ) : MergeConflictsResolver(git, pullResult) {
    override fun resolveAndSave(note: Note, suggestion: FileSuggestion, commitRename: () -> Unit?) {
      val noteFile = suggestion.suggestedFile
      val notePath = suggestion.suggestedFilePath
      val oldPath = suggestion.oldFilePath

      // Git makes it easy to handle merge conflicts, but automating it for
      // the user is going to be a challenge. If the same note was modified
      // from different devices, Press duplicates them: note.md & note_2.md.
      // This will also cover non-.md files.
      //
      // Duplicating the local note is fewer work in the current design than
      // remote. If the local note is being edited right now, Press will
      // close and re-open the note.
      if (notePath in pulledPaths) {
        if (noteFile.equalsContent(note.content).not()) {
          // File's content is going to change in a conflicting way. Duplicate the note.
          val newName = register.generateNameFor(note, canUseExisting = false)
          log("   duplicating to '<same parent>/$newName' to resolve merge conflict")
          noteFile.copy(newName).write(note.conflictedContent)
          mergeConflicts.add(note.id)
        } else {
          log("   skipping (same content)")
        }

      } else if (oldPath in pulledPaths) {
        // Old path was updated on remote, but deleted (on rename) locally. By not
        // accepting the rename, this file will later be processed as a new note.
        log("   creating as a new note to resolve merge conflict (old path = '$oldPath')")
        noteFile.write(note.conflictedContent)

      } else {
        log("   creating/updating")
        suggestion.acceptRename?.let {
          it.invoke()
          commitRename()
        }
        noteFile.write(note.content)
      }
    }

    private val Note.conflictedContent
      get() = HeadingAndBody.prefixHeading(
          content = content,
          prefix = "${strings.sync.conflicted_note_heading_prefix}: "
      )
  }

  @Suppress("NAME_SHADOWING")
  private fun SyncTransaction.processCommits(pullResult: PullResult) {
    if (pullResult.headBefore == git.headCommit()) {
      log("Nothing to process.")
      return
    }

    val to = git.headCommit()!!
    val from = git.commonAncestor(first = pullResult.headBefore, second = to)

    log("\nProcessing commits from ${from?.sha1?.abbreviated} to ${to.sha1.abbreviated}")
    val commits = git.commitsBetween(from = from, toInclusive = to)
    commits.forEach { log(" • ${it.sha1.abbreviated} - ${it.message.lines().first()}") }

    val diffSinceLastSync = git.changesBetween(from, to)

    log("\nProcessing changes (${diffSinceLastSync.size}):")
    if (diffSinceLastSync.isNotEmpty()) {
      log(diffSinceLastSync.flattenToString())
    }

    val diffPathTimestamps = commits.pathTimestamps(git)
    for (diff in diffSinceLastSync.filterNoteChanges()) {
      dbOperations += when (diff) {
        is Copy,
        is Rename,
          // Renaming of note files are ignored. Press
          // generates a name as per the note's heading.
        is Add, is Modify -> {
          val file = File(directory, diff.path)
          val content = file.read()

          val oldPath = if (diff is Rename) diff.fromPath else null
          val record = register.recordFor(diff.path, oldPath = oldPath)
              ?: register.createNewRecordFor(file, id = NoteId.generate())

          val noteId = record.noteId
          val isArchived = record.noteFolder == "archived"
          val isNewNote = !noteQueries.exists(noteId).executeAsOne()
          val commitTime = diffPathTimestamps[diff.path]!!

          if (isNewNote) {
            log("\nCreating new note $noteId for (${diff.path}), isArchived? $isArchived")
          } else {
            log("\nUpdating $noteId (${diff.path}), isArchived? $isArchived")
          }

          if (isNewNote) {
            Runnable {
              noteQueries.insert(
                  id = noteId,
                  content = content,
                  createdAt = commitTime,
                  updatedAt = commitTime
              )
              noteQueries.setArchived(
                  id = noteId,
                  isArchived = isArchived,
                  updatedAt = commitTime
              )
              noteQueries.updateSyncState(
                  ids = listOf(noteId),
                  syncState = IN_FLIGHT
              )
            }
          } else {
            Runnable {
              noteQueries.updateContent(
                  id = noteId,
                  content = content,
                  updatedAt = commitTime
              )
              noteQueries.setArchived(
                  id = noteId,
                  isArchived = isArchived,
                  updatedAt = commitTime
              )
              noteQueries.updateSyncState(
                  ids = listOf(noteId),
                  syncState = IN_FLIGHT
              )
            }
          }
        }
        is Delete -> {
          // Handled later.
          Runnable {}
        }
      }
    }

    if (git.isStagingAreaDirty()) {
      git.commitAll(
          message = "Create new file name records",
          timestamp = UtcTimestamp(clock)
      )
    }

    // Deleted notes will need to be processed commit-wise. If a note was updated _and_ deleted
    // in pulled changes then its diff entry will not match the diff entry of its filename record.
    if (diffSinceLastSync.filterNoteChanges().any { it is Delete }) {
      log("\nRe-processing changes to read deletions")

      val restoreToBranch = git.currentBranch()
      for ((commit, diffs) in commits.changes(git)) {
        git.checkout(commit)

        for (diff in diffs.filterNoteChanges().filterIsInstance<Delete>()) {
          val noteId = register.noteIdFor(diff.path)
          log(" • '${diff.path}' deleted in ${commit.sha1.abbreviated} (${commit.message})")

          if (noteId != null) {
            log("   permanently deleting $noteId")
            dbOperations += Runnable {
              noteQueries.markAsPendingDeletion(noteId)
              noteQueries.updateSyncState(ids = listOf(noteId), syncState = IN_FLIGHT)
              noteQueries.deleteNote(noteId)
            }
          } else {
            // Either this commit has already been processed
            // earlier or this isn't a note (e.g., README.md).
            log("   can't find file")
          }
        }
      }
      git.checkout(restoreToBranch.name)
    }
  }

  private fun SyncTransaction.push(pullResult: PullResult) {
    val remote = readConfig()!!.remote.remote
    check(git.currentBranch().name == remote.defaultBranch) { "Not on the default branch" }
    check(!git.isStagingAreaDirty()) { "Expected staging area to be clean before pushing" }

    if (pullResult.headAfter == git.headCommit()) {
      noteQueries.swapSyncStates(old = listOf(IN_FLIGHT), new = SYNCED)
      log("\nNothing to push.")

    } else {
      log("\nPushing changes")
      loggers.onSyncComplete()
      git.commitAll(
          message = "Update sync logs",
          timestamp = UtcTimestamp(clock)
      )

      val pushResult = git.push()
      if (pushResult is Failure) {
        // Merge conflicts can only be avoided if we're always on the latest version of upstream.
        // If push fails, abort this sync. Any unsynced changes will be reset on next sync.
        error("Couldn't push: $pushResult")
      }
    }

    if (dbOperations.isNotEmpty()) {
      noteQueries.transaction {
        dbOperations.forEach { it.run() }
      }
      mergeConflicts.clear()
    }

    val savedNotes = noteQueries.allNotes().executeAsList()
    register.pruneStaleRecords(savedNotes)
    if (git.isStagingAreaDirty()) {
      git.commitAll(
          message = "Prune stale file name records",
          timestamp = UtcTimestamp(clock)
      )
    }

    noteQueries.swapSyncStates(
        old = listOf(IN_FLIGHT),
        new = SYNCED
    )
    configQueries.update(
        folder = folder,
        lastSyncedAt = clock.nowUtc(),
        lastPushedSha1 = git.headCommit()!!.sha1.value
    )
  }

  private fun log(message: String) = loggers.log(message)

  private fun List<GitCommit>.changes(git: GitRepository): List<Pair<GitCommit, GitTreeDiff>> {
    return zipWithNext(initial = null).map { (prevCommit, currentCommit) ->
      currentCommit!! to (git.changesBetween(prevCommit, currentCommit))
    }
  }

  private fun List<GitCommit>.pathTimestamps(git: GitRepository): Map<String, DateTime> {
    return changes(git).flatMap { (commit, changes) ->
      // Press stores updated-at timestamp of notes in each commit.
      changes.map { it.path to commit.dateTime }
    }.toMap()
  }

  private fun readConfig(): FolderSyncConfig? {
    return configQueries.select(folder).executeAsOneOrNull()
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

private fun GitTreeDiff.flattenToString(): String {
  return joinToString(prefix = " • ", separator = "\n • ")
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

@OptIn(ExperimentalStdlibApi::class)
fun <T> List<T>.zipWithNext(initial: T): List<Pair<T,T>> {
  return (listOf(initial) + this).zipWithNext()
}

private val Unit.exhaustive: Unit
  get() = this
