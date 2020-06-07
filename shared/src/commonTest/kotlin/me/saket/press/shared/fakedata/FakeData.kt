package me.saket.press.shared.fakedata

import com.soywiz.klock.DateTime
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.DateTimeAdapter
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.time.Clock
import me.saket.press.shared.time.FakeClock
import kotlin.random.Random

fun fakeNote(
  localId: Long = Random.Default.nextLong(),
  noteId: NoteId = NoteId.generate(),
  content: String,
  clock: Clock = FakeClock(),
  createdAt: DateTime = clock.nowUtc(),
  updatedAt: DateTime = clock.nowUtc(),
  archivedAt: DateTime? = null,
  deletedAt: DateTime? = null
): Note.Impl {
  return Note.Impl(
      localId = localId,
      uuid = noteId,
      content = content,
      createdAt = createdAt,
      updatedAt = updatedAt,
      archivedAtString = archivedAt?.let { DateTimeAdapter.encode(it) },
      deletedAtString = deletedAt?.let { DateTimeAdapter.encode(it) }
  )
}
