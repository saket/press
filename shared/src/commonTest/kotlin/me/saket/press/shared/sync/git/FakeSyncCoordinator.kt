package me.saket.press.shared.sync.git

import co.touchlab.stately.concurrency.AtomicBoolean
import com.badoo.reaktive.completable.completableOfEmpty
import me.saket.press.shared.sync.SyncCoordinator

class FakeSyncCoordinator : SyncCoordinator {
  override fun start() = Unit
  override fun syncWithResult() = completableOfEmpty()

  val triggered = AtomicBoolean(false)
  override fun trigger() {
    triggered.value = true
  }
}
