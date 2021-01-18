package me.saket.press.shared.syncer

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.badoo.reaktive.test.scheduler.TestScheduler
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import me.saket.press.shared.FakeSchedulers
import kotlin.test.Test

class RealSyncCoordinatorTest {
  private val syncer = FakeSyncer()
  private val syncEvery = 5.seconds
  private val computationScheduler = TestScheduler()

  private val coordinator = RealSyncCoordinator(
    syncer = syncer,
    schedulers = FakeSchedulers(computation = computationScheduler),
    syncEvery = syncEvery
  )

  @Test fun `a second sync should only be scheduled once the first sync finishes`() {
    coordinator.start()
    coordinator.trigger()

    // trigger() should immediately start a new sync.
    assertThat(syncer.syncRequestCount.get()).isEqualTo(1)

    // Timer to start the next sync should have not started when trigger() was called. It'll
    // otherwise result in a new sync request before the last one gets a chance to finish.
    computationScheduler.advanceTimeBy(syncEvery)
    assertThat(syncer.syncRequestCount.get()).isEqualTo(1)

    syncer.finishSync()

    computationScheduler.advanceTimeBy(syncEvery)
    assertThat(syncer.syncRequestCount.get()).isEqualTo(2)
  }

  @Test fun `triggering a sync cancels any ongoing sync`() {
    coordinator.start()
    coordinator.trigger()
    assertThat(syncer.syncRequestCount.get()).isEqualTo(1)

    coordinator.trigger()
    assertThat(syncer.syncRequestCount.get()).isEqualTo(2)

    // If the previous sync wasn't canceled, then the second trigger() will
    // cause *2* new syncs to be scheduled instead of 1 when this one finishes.
    syncer.finishSync()

    computationScheduler.advanceTimeBy(syncEvery)
    assertThat(syncer.syncRequestCount.get()).isEqualTo(3)
  }

  private fun TestScheduler.advanceTimeBy(span: TimeSpan) {
    timer.advanceBy(span.millisecondsLong)
  }
}
