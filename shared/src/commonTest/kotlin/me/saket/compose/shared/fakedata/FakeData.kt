package me.saket.compose.shared.fakedata

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import me.saket.compose.data.shared.Note
import time.FakeClock

private val clock = FakeClock()

fun fakeNote(
  localId: Long = -1L,
  uuid: Uuid = uuid4(),
  content: String,
  createdAt: DateTime = clock.nowUtc(),
  updatedAt: DateTime = clock.nowUtc(),
  deletedAt: DateTime? = null
): Note {
  return Note.Impl(
      localId = localId,
      uuid = uuid,
      content = content,
      createdAt = createdAt,
      updatedAt = updatedAt,
      deletedAt = deletedAt
  )
}