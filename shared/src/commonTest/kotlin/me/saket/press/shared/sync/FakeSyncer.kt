package me.saket.press.shared.sync

import co.touchlab.stately.concurrency.AtomicBoolean
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOf
import me.saket.press.shared.sync.Syncer.Status2.Disabled

class FakeSyncer : Syncer() {
  private val _syncCalled = AtomicBoolean(false)
  val syncCalled get() = _syncCalled.value

  override fun status(): Observable<Status2> = observableOf(Disabled)
  override fun disable() = Unit

  override fun sync() {
    _syncCalled.value = true
  }
}
