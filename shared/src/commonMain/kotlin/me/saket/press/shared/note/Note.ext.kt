package me.saket.press.shared.note

import com.soywiz.klock.DateTime
import me.saket.press.data.shared.Note
import me.saket.press.data.shared.NoteQueries
import me.saket.press.shared.db.DateTimeAdapter
import me.saket.press.shared.db.NoteId

// Workaround until https://youtrack.jetbrains.com/issue/KT-35234 is fixed.
val Note.deletedAt get() = deletedAtString?.let { DateTimeAdapter.decode(it) }

fun NoteQueries.markAsDeleted(uuid: NoteId, deletedAt: DateTime) {
  markAsDeleted(uuid = uuid, deletedAtString = DateTimeAdapter.encode(deletedAt))
}

val Note.archivedAt get() = archivedAtString?.let { DateTimeAdapter.decode(it) }
