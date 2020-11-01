package me.saket.press.shared.note

import co.touchlab.stately.collections.IsoMutableList
import co.touchlab.stately.concurrency.AtomicInt
import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.completable.observeOn
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observableFromFunction
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.test.scheduler.TestScheduler
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.util.Optional
import me.saket.press.shared.util.toOptional
import kotlin.test.assertNull
import kotlin.test.assertTrue

// TODO: this class is stupid. Get rid of fake repository in favor of in-memory SQL.
class FakeNoteRepository : NoteRepository {
  val savedNotes = IsoMutableList<Note>()

  private val _updateCount = AtomicInt(0)
  val updateCount: Int get() = _updateCount.get()
  private val scheduler = TestScheduler()

  private fun findNote(noteId: NoteId) = savedNotes.find { it.id == noteId }

  override fun note(id: NoteId): Observable<Optional<Note>> {
    return observableFromFunction { findNote(id).toOptional() }.observeOn(scheduler)
  }

  override fun visibleNotes(): Observable<List<Note>> =
    observableFromFunction { savedNotes }.observeOn(scheduler)

  override fun create(vararg insertNotes: InsertNote): Completable {
    return completableFromFunction {
      for (note in insertNotes) {
        assertNull(findNote(note.id))
        savedNotes += fakeNote(id = note.id, content = note.content)
      }
    }.observeOn(scheduler)
  }

  override fun update(id: NoteId, content: String): Completable {
    return completableFromFunction {
      assertTrue(savedNotes.remove(findNote(id)))
      savedNotes += fakeNote(id = id, content = content)
      _updateCount.addAndGet(1)
    }.observeOn(scheduler)
  }

  override fun markAsPendingDeletion(id: NoteId): Completable {
    return completableFromFunction {
      val existingNote = findNote(id)!!
      assertTrue(savedNotes.remove(existingNote))
      savedNotes += existingNote.copy(isPendingDeletion = true)
      _updateCount.addAndGet(1)
    }.observeOn(scheduler)
  }

  override fun markAsArchived(id: NoteId): Completable {
    return completableFromFunction {
      val existingNote = findNote(id)!!
      assertTrue(savedNotes.remove(existingNote))
      savedNotes += existingNote.copy(isArchived = true)
      _updateCount.addAndGet(1)
    }.observeOn(scheduler)
  }
}
