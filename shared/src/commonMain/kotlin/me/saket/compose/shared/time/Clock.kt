package me.saket.compose.shared.time

import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeTz

interface Clock {
  fun nowUtc(): DateTime
  fun nowLocal(): DateTimeTz
}

class RealClock : Clock {

  override fun nowUtc(): DateTime {
    return DateTime.now()
  }

  override fun nowLocal(): DateTimeTz {
    return DateTimeTz.nowLocal()
  }
}
