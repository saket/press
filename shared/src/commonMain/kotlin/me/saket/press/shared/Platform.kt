package me.saket.press.shared

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4

expect object Platform {
  val name: String
}

fun generateUuid(): Uuid {
  return uuid4()
}
