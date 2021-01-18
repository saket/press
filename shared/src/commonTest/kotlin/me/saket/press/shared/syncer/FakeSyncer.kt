package me.saket.press.shared.syncer

import co.touchlab.stately.concurrency.AtomicInt
import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.observable.firstOrError
import com.badoo.reaktive.observable.observableOfNever
import com.badoo.reaktive.single.asCompletable
import com.badoo.reaktive.subject.publish.PublishSubject
import me.saket.press.shared.syncer.git.File

class FakeSyncer : Syncer() {
  private var sync = PublishSubject<Unit>()
  var syncRequestCount = AtomicInt(0)
  val status = PublishSubject<Status>()

  override fun syncCompletable(): Completable {
    syncRequestCount.incrementAndGet()
    return sync.firstOrError().asCompletable()
  }

  fun finishSync() = sync.onNext(Unit)

  override fun status() = status

  override val directory: File get() = TODO()
  override fun sync() = error("syncCompletable() should be used")
  override fun disable() = Unit
}
