package me.saket.press.shared.time

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.TimezoneOffset

class FakeClock(
  private var utc: DateTime = DateTime.EPOCH,
  private val offset: TimezoneOffset = TimezoneOffset.local(utc)
) : Clock {

  override fun nowUtc() = utc

  override fun nowLocal() = utc.toOffset(offset)

  fun advanceTimeBy(span: TimeSpan) {
    utc = utc.plus(span)
  }
}
