package me.saket.press.shared.sync

import com.badoo.reaktive.observable.Observable
import com.soywiz.klock.DateTime
import me.saket.press.shared.sync.git.service.GitRepositoryInfo

/** Syncs notes with a remote destination. */
abstract class Syncer {

  internal abstract fun status(): Observable<Status>

  /**
   * Called every time a note's content is updated,
   * including when it's created for the first time.
   */
  internal abstract fun sync()

  abstract fun disable()

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
inline class LastPushedSha1(val sha1: String)
