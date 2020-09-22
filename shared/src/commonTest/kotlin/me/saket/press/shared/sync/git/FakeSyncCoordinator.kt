package me.saket.press.shared.sync.git

import com.badoo.reaktive.completable.completableOfEmpty
import me.saket.press.shared.sync.SyncCoordinator

class FakeSyncCoordinator : SyncCoordinator {
  override fun start() = Unit
  override fun trigger() = Unit
  override fun syncWithResult() = completableOfEmpty()
}
