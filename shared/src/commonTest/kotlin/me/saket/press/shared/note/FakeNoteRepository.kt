package me.saket.press.shared.note

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableFromFunction
import com.benasher44.uuid.Uuid
import me.saket.press.data.shared.Note
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.util.Optional
import me.saket.press.shared.util.toOptional
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeNoteRepository : NoteRepository {
  val savedNotes = mutableListOf<Note.Impl>()
  var updateCount = 0

  private fun findNote(noteUuid: Uuid) = savedNotes.find { it.uuid == noteUuid }

  override fun note(noteUuid: Uuid): Observable<Optional<Note>> {
    return observableFromFunction { findNote(noteUuid).toOptional() }
  }

  override fun notes(includeEmptyNotes: Boolean): Observable<List<Note>> =
    observableFromFunction {
      when {
        includeEmptyNotes -> savedNotes
        else -> savedNotes.filter { it.content.isNotEmpty() }
      }
    }

  override fun create(vararg insertNotes: InsertNote): Completable {
    return completableFromFunction {
      for (note in insertNotes) {
        assertNull(findNote(note.uuid))
        savedNotes += fakeNote(uuid = note.uuid, content = note.content)
      }
    }
  }

  override fun update(noteUuid: Uuid, content: String): Completable {
    return completableFromFunction {
      assertTrue(savedNotes.remove(findNote(noteUuid)))
      savedNotes += fakeNote(uuid = noteUuid, content = content)
      updateCount++
    }
  }

  override fun markAsDeleted(noteUuid: Uuid): Completable {
    return completableFromFunction {
      val existingNote = findNote(noteUuid)!!
      assertTrue(savedNotes.remove(existingNote))
      savedNotes += existingNote.copy(deletedAtString = "current_time")
      updateCount++
    }
  }
}
