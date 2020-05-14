package me.saket.press.shared.note

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.observable.Observable
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.util.Optional

interface NoteRepository {
  fun note(id: NoteId): Observable<Optional<Note>>
  fun notes(): Observable<List<Note>>
  fun create(vararg insertNotes: InsertNote): Completable
  fun update(id: NoteId, content: String): Completable
  fun markAsDeleted(id: NoteId): Completable
  fun markAsArchived(id: NoteId): Completable

  fun create(id: NoteId, content: String): Completable {
    return create(InsertNote(id, content))
  }
}
