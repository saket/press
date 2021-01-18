package me.saket.press.shared.syncer

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.andThen
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.completable.onErrorComplete
import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.switchMapCompletable
import com.badoo.reaktive.subject.publish.PublishSubject
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.completableTimer
import me.saket.press.shared.rx.takeUntil
import me.saket.press.shared.syncer.Syncer.Status.Disabled

/**
 * Syncs can be triggered from multiple places at different times. This class
 * coordinates between them and maintains a timer for syncing periodically.
 */
interface SyncCoordinator {
  fun start()
  fun trigger()
  fun syncWithResult(): Completable
}

class RealSyncCoordinator(
  private val syncer: Syncer,
  private val schedulers: Schedulers,
  private val syncEvery: TimeSpan = 30.seconds
) : SyncCoordinator {
  private val triggers = PublishSubject<Unit>()

  override fun start() {
    triggers.switchMapCompletable {
      syncWithResult()
        .andThen(completableTimer(syncEvery, schedulers.computation))
        .andThen(completableFromFunction { trigger() })
    }.subscribe()
  }

  override fun trigger() {
    triggers.onNext(Unit)
  }

  override fun syncWithResult(): Completable {
    return syncer.syncCompletable()
      .takeUntil(syncer.status().ofType<Disabled>())
      .onErrorComplete()
  }
}
