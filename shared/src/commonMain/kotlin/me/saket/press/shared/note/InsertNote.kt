package me.saket.press.shared.note

import com.soywiz.klock.DateTime
import me.saket.press.shared.db.NoteId

/**
 * [createdAt] when null, the current time is used.
 */
data class InsertNote internal constructor(
  val id: NoteId,
  val content: String,
  val createdAt: DateTime? = null
)
