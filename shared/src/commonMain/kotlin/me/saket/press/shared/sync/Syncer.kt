package me.saket.press.shared.sync

import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.soywiz.klock.DateTime
import kotlinx.serialization.Serializable
import me.saket.press.shared.db.DateTimeAdapter
import me.saket.press.shared.sync.Syncer.Status

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
    object InFlight : Status()
    object Failed : Status()
    data class Idle(val lastSyncedAt: DateTime?) : Status()
  }
}

fun Syncer.syncCompletable() = completableFromFunction { sync() }
