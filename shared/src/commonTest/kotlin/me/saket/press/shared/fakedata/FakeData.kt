package me.saket.press.shared.fakedata

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.DateTimeAdapter
import me.saket.press.shared.time.FakeClock
import kotlin.random.Random

private val clock = FakeClock()

fun fakeNote(
  localId: Long = Random.Default.nextLong(),
  uuid: Uuid = uuid4(),
  content: String,
  createdAt: DateTime = clock.nowUtc(),
  updatedAt: DateTime = clock.nowUtc(),
  archivedAt: DateTime? = null,
  deletedAt: DateTime? = null
): Note.Impl {
  return Note.Impl(
      localId = localId,
      uuid = uuid,
      content = content,
      createdAt = createdAt,
      updatedAt = updatedAt,
      archivedAtString = archivedAt?.let{ DateTimeAdapter.encode(it) },
      deletedAtString = deletedAt?.let{ DateTimeAdapter.encode(it) }
  )
}
