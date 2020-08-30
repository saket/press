package me.saket.press.shared.sync

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.completable.doOnBeforeError
import com.badoo.reaktive.completable.onErrorComplete
import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.switchMap
import com.badoo.reaktive.subject.publish.PublishSubject
import com.soywiz.klock.seconds
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.observableInterval

/**
 * Syncs can be triggered from multiple places at different times. This class
 * coordinates between them and maintains a timer for syncing periodically.
 */
class SyncCoordinator(
  private val syncer: Syncer,
  private val schedulers: Schedulers
) {
  private val triggers = PublishSubject<Unit>()

  fun start() {
    triggers.switchMap { observableInterval(0, 30.seconds, schedulers.computation) }
        .flatMapCompletable { syncWithResult() }
        .subscribe()
  }

  fun trigger() {
    triggers.onNext(Unit)
  }

  internal fun syncWithResult(): Completable {
    return completableFromFunction { syncer.sync() }
        .doOnBeforeError { println(it.message) }
        .onErrorComplete()
  }
}
