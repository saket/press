package me.saket.press.shared.sync

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.completable.onErrorComplete
import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.observable.doOnBeforeNext
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.subject.publish.PublishSubject
import com.soywiz.klock.seconds
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.observableInterval
import me.saket.press.shared.rx.takeUntil
import me.saket.press.shared.sync.Syncer.Status.Disabled

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
  private val schedulers: Schedulers
) : SyncCoordinator {
  private val triggers = PublishSubject<Unit>()

  override fun start() {
    triggers.switchMap { observableInterval(0, 30.seconds, schedulers.computation) }
        .flatMapCompletable { syncWithResult() }
        .subscribe()
  }

  override fun trigger() {
    triggers.onNext(Unit)
  }

  override fun syncWithResult(): Completable {
    return completableFromFunction { syncer.sync() }
        .takeUntil(syncer.status().ofType<Disabled>())
        .onErrorComplete()
  }
}
