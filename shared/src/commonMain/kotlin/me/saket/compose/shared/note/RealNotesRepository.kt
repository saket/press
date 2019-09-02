package me.saket.compose.shared.note

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOfEmpty

class RealNotesRepository : NoteRepository {

  override fun notes(): Observable<List<Note>> {
    return observableOfEmpty()
  }
}