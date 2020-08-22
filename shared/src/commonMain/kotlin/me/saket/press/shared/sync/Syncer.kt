package me.saket.press.shared.sync

import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.soywiz.klock.DateTime
import kotlinx.serialization.Serializable
import me.saket.press.shared.db.DateTimeAdapter
import me.saket.press.shared.sync.Syncer.Status

/** Syncs notes with a remote destination. */
abstract class Syncer {

  internal abstract fun status(): Observable<Pair<Status, LastSyncedAt?>>

  /**
   * Called every time a note's content is updated,
   * including when it's created for the first time.
   */
  internal abstract fun sync()

  abstract fun disable()

  sealed class Status {
    object Disabled : Status()
    object InFlight : Status()
    object Failed : Status()
    object Idle : Status()
  }
}

inline class LastSyncedAt(val value: DateTime)

fun Syncer.syncCompletable() = completableFromFunction { sync() }
