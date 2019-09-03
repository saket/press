package me.saket.compose.shared.note

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.publish.publishSubject

class FakeNoteRepository : NoteRepository {

  val noteSubject = publishSubject<List<Note>>()
  override fun notes(): Observable<List<Note>> = noteSubject
}