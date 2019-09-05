package me.saket.compose.shared.note

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOf
import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import me.saket.compose.data.shared.Note

class RealNotesRepository : NoteRepository {

  override fun notes(): Observable<List<Note>> {
    val notes = (0..10).map {
      Note.Impl(
          id = uuid4(),
          title = "Nicolas Cage",
          body = "Our national treasure",
          createdAt = DateTime.EPOCH,
          updatedAt = DateTime.EPOCH,
          deletedAt = null
      )
    }
    return observableOf(notes)
  }
}