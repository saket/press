package me.saket.press.shared.sync

import com.badoo.reaktive.completable.Completable
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
  // todo: remove rx
  internal abstract fun sync(): Completable

  abstract fun disable(): Completable

  @Serializable
  sealed class Status {
    @Serializable
    object Disabled : Status()

    @Serializable
    object InFlight : Status()

    @Serializable
    object Failed : Status()

    @Serializable
    @Suppress("DataClassPrivateConstructor")
    data class Idle private constructor(internal val lastSyncedAtString: String?) : Status() {
      constructor(lastSyncedAt: DateTime?) : this(lastSyncedAt?.let(DateTimeAdapter::encode))
    }
  }
}

// because kotlinx serialization fails majestically for inline classes (DateTime in this case).
val Status.Idle.lastSyncedAt: DateTime?
  get() {
    // todo: use ?.let{} after https://youtrack.jetbrains.com/issue/KT-35234 is fixed
    return when (lastSyncedAtString) {
      null -> null
      else -> DateTimeAdapter.decode(lastSyncedAtString)
    }
  }
