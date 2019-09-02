package me.saket.compose.shared.note

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOf

class RealNotesRepository : NoteRepository {

  override fun notes(): Observable<List<Note>> {
    val notes = (0..10).map { Note(
        id = it.toLong(),
        title = "Title $it",
        body = "Body $it"
    ) }
    return observableOf(notes)
  }
}