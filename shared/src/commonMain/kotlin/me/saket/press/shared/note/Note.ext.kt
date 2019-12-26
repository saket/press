package me.saket.press.shared.note

import me.saket.press.data.shared.Note
import me.saket.press.shared.db.DateTimeAdapter

// Workaround until https://youtrack.jetbrains.com/issue/KT-35234 is fixed.
val Note.deletedAt get() = deletedAtString?.let { DateTimeAdapter.decode(it) }

val Note.archivedAt get() = archivedAtString?.let { DateTimeAdapter.decode(it) }
