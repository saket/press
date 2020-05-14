package me.saket.press.shared

import com.benasher44.uuid.Uuid
import me.saket.press.shared.db.NoteId

expect object Platform {
  val name: String
}

fun generateUuid(): Uuid {
  return NoteId.generate().value
}
