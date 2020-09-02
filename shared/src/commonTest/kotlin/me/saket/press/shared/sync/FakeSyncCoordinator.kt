package me.saket.press.shared.sync

import co.touchlab.stately.concurrency.AtomicBoolean
import com.badoo.reaktive.completable.completableOfEmpty

class FakeSyncCoordinator : SyncCoordinator {
  private val _syncTriggered = AtomicBoolean(false)
  val syncTriggered get() = _syncTriggered.value

  override fun trigger() {
    _syncTriggered.value = true
  }

  override fun start() = Unit
  override fun syncWithResult() = completableOfEmpty()
}
