package me.saket.press.shared.note

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.observable.Observable
import com.benasher44.uuid.Uuid
import me.saket.press.data.shared.Note
import me.saket.press.shared.util.Optional

interface NoteRepository {
  fun note(noteUuid: Uuid): Observable<Optional<Note>>
  fun notes(): Observable<List<Note>>
  fun create(noteUuid: Uuid, content: String): Completable
  fun update(noteUuid: Uuid, content: String): Completable
  fun markAsDeleted(noteUuid: Uuid): Completable
}
