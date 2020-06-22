package me.saket.press.shared.fakedata

import com.soywiz.klock.DateTime
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.sync.SyncState
import me.saket.press.shared.sync.SyncState.PENDING
import me.saket.press.shared.time.Clock
import me.saket.press.shared.time.FakeClock
import kotlin.random.Random

// todo: rename [noteId] to [id].
fun fakeNote(
  content: String,
  localId: Long = Random.Default.nextLong(),
  noteId: NoteId = NoteId.generate(),
  clock: Clock = FakeClock(),
  createdAt: DateTime = clock.nowUtc(),
  updatedAt: DateTime = clock.nowUtc(),
  isArchived: Boolean = false,
  isPendingDeletion: Boolean = false,
  syncState: SyncState = PENDING
): Note.Impl {
  return Note.Impl(
      localId = localId,
      id = noteId,
      content = content,
      createdAt = createdAt,
      updatedAt = updatedAt,
      isArchived = isArchived,
      isPendingDeletion = isPendingDeletion,
      syncState = syncState
  )
}
