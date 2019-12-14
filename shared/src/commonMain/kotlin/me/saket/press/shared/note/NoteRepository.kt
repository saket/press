package me.saket.press.shared.note

import com.badoo.reaktive.annotations.EventsOnIoScheduler
import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.observable.Observable
import com.benasher44.uuid.Uuid
import me.saket.press.data.shared.Note
import me.saket.press.shared.util.Optional

interface NoteRepository {
  @EventsOnIoScheduler
  fun note(noteUuid: Uuid): Observable<Optional<Note>>

  @EventsOnIoScheduler
  fun notes(includeEmptyNotes: Boolean): Observable<List<Note>>

  @EventsOnIoScheduler
  fun create(vararg insertNotes: InsertNote): Completable

  @EventsOnIoScheduler
  fun update(noteUuid: Uuid, content: String): Completable

  @EventsOnIoScheduler
  fun markAsDeleted(noteUuid: Uuid): Completable

  @EventsOnIoScheduler
  fun create(noteUuid: Uuid, content: String): Completable {
    return create(InsertNote(noteUuid, content))
  }
}
