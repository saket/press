package me.saket.press.shared.sync

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.observable.Observable
import com.soywiz.klock.DateTime

/** Syncs notes with a remote destination. */
abstract class Syncer {

  internal abstract fun status(): Observable<Status>

  /**
   * Called every time a note's content is updated,
   * including when it's created for the first time.
   */
  internal abstract fun sync(): Completable

  sealed class Status {
    object Disabled : Status()
    object InFlight : Status()
    data class Idle(val lastSyncedAt: DateTime?) : Status()
  }
}
