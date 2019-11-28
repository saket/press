package me.saket.press.shared.note

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableOf
import com.benasher44.uuid.Uuid
import me.saket.press.data.shared.Note
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.util.Optional
import me.saket.press.shared.util.toOptional
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeNoteRepository : NoteRepository {
  val savedNotes = mutableListOf<Note>()

  private fun findNote(noteUuid: Uuid) = savedNotes.find { it.uuid == noteUuid }

  override fun note(noteUuid: Uuid): Observable<Optional<Note>> =
    observableOf(findNote(noteUuid).toOptional())

  override fun notes(): Observable<List<Note>> =
    observableOf(savedNotes)

  override fun create(vararg insertNotes: InsertNote): Completable {
    return completable {
      for (note in insertNotes) {
        assertNull(findNote(note.uuid))
        savedNotes += fakeNote(uuid = note.uuid, content = note.content)
      }
    }
  }

  override fun update(noteUuid: Uuid, content: String): Completable {
    return completable {
      assertTrue(savedNotes.remove(findNote(noteUuid)))
      savedNotes += fakeNote(uuid = noteUuid, content = content)
    }
  }

  override fun markAsDeleted(noteUuid: Uuid): Completable {
    return completable {
      assertNotNull(findNote(noteUuid))
      savedNotes.remove(findNote(noteUuid))
    }
  }
}
