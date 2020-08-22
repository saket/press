package me.saket.press.shared.sync

import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.soywiz.klock.DateTime
import me.saket.press.shared.sync.git.service.GitRepositoryInfo

/** Syncs notes with a remote destination. */
abstract class Syncer {

  internal abstract fun status(): Observable<Status2>

  /**
   * Called every time a note's content is updated,
   * including when it's created for the first time.
   */
  internal abstract fun sync()

  abstract fun disable()

  sealed class Status2 {
    object Disabled : Status2()

    data class Enabled(
      val lastOp: LastOp,
      val syncingWith: GitRepositoryInfo,
      val lastSyncedAt: LastSyncedAt?
    ) : Status2()

    enum class LastOp {
      InFlight,
      Failed,
      Idle
    }
  }
}

inline class LastSyncedAt(val value: DateTime)

fun Syncer.syncCompletable() = completableFromFunction { sync() }
