package me.saket.press.shared.syncer

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.soywiz.klock.DateTime
import me.saket.press.shared.syncer.git.File
import me.saket.press.shared.syncer.git.service.GitRepositoryInfo

/** Syncs notes with a remote destination. */
abstract class Syncer {

  /**
   * Directory where the notes are stored. Currently only used for logging size
   * to find out how large the folder can grow for GitSyncer. It may be useful to
   * truncate the git history in the future. A full history isn't needed on clients.
   */
  abstract val directory: File

  /**
   * Called every time a note's content is updated,
   * including when it's created for the first time.
   */
  internal abstract fun sync()

  abstract fun disable()

  internal abstract fun status(): Observable<Status>

  internal open fun syncCompletable(): Completable =
    completableFromFunction { sync() }

  sealed class Status {
    object Disabled : Status()

    data class Enabled(
      val lastOp: LastOp,
      val syncingWith: GitRepositoryInfo,
      val lastSyncedAt: LastSyncedAt?
    ) : Status()

    enum class LastOp {
      InFlight,
      Failed,
      Idle
    }
  }
}

inline class LastSyncedAt(val value: DateTime)
