package me.saket.press.shared.time

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.TimezoneOffset
import me.saket.press.shared.util.FreezableAtomicReference

class FakeClock : Clock {

  private val utc = FreezableAtomicReference(DateTime.EPOCH)
  private val offset = FreezableAtomicReference(TimezoneOffset.local(DateTime.EPOCH))

  override fun nowUtc() = utc.value

  override fun nowLocal() = utc.value.toOffset(offset.value)

  fun advanceTimeBy(span: TimeSpan) {
    utc.value = utc.value.plus(span)
  }
}
