package me.saket.press.shared.time

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.TimezoneOffset
import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import me.saket.press.shared.util.FreezableAtomicReference

class FakeClock : Clock {

  private val utc = FreezableAtomicReference(DateTime.EPOCH)
  private val offset = FreezableAtomicReference(TimezoneOffset.local(DateTime.EPOCH))

  init {
    // Start with a real time so that
    // tests aren't living in Jan 1, 1970.
    utc.value = RealClock().nowUtc()

    // Git and sql numbers are precise upto 1 second.
    // Throw away milliseconds to screw up tests.
    advanceTimeBy(1.seconds - nowUtc().milliseconds.milliseconds)
  }

  override fun nowUtc() = utc.value

  override fun nowLocal() = utc.value.toOffset(offset.value)

  fun advanceTimeBy(span: TimeSpan) {
    utc.value = utc.value.plus(span)
  }
}
