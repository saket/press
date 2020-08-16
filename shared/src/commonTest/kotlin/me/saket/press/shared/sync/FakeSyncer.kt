package me.saket.press.shared.sync

import co.touchlab.stately.concurrency.AtomicBoolean
import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOf
import me.saket.press.shared.sync.Syncer.Status.Disabled

class FakeSyncer : Syncer() {
  private val _syncCalled = AtomicBoolean(false)
  val syncCalled get() = _syncCalled.value

  override fun status(): Observable<Status> = observableOf(Disabled)
  override fun disable(): Completable = completableFromFunction { }

  override fun sync(): Completable = completableFromFunction {
    _syncCalled.value = true
  }
}
