package me.saket.compose.shared.note

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOf
import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTimeTz
import me.saket.compose.ComposeDatabase
import me.saket.compose.data.shared.Note

class RealNotesRepository(db: ComposeDatabase) : NoteRepository {

  override fun notes(): Observable<List<Note>> {
    val notes = (0..10).map {
      Note.Impl(
          id = uuid4(),
          title = "Nicolas Cage",
          body = "Our national treasure",
          createdAt = DateTimeTz.nowLocal(),
          updatedAt = DateTimeTz.nowLocal(),
          deletedAt = null
      )
    }
    return observableOf(notes)
  }
}