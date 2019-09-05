package me.saket.compose.shared.note

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.publishSubject
import me.saket.compose.data.shared.Note

class FakeNoteRepository : NoteRepository {

  val noteSubject = publishSubject<List<me.saket.compose.data.shared.Note>>()
  override fun notes(): Observable<List<Note>> = noteSubject
}