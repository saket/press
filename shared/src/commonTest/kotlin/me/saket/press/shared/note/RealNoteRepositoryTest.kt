package me.saket.press.shared.note

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.badoo.reaktive.scheduler.trampolineScheduler
import com.badoo.reaktive.test.completable.test
import com.soywiz.klock.seconds
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.time.FakeClock
import kotlin.test.Test

class RealNoteRepositoryTest : BaseDatabaeTest() {

  private val clock = FakeClock()
  private val noteQueries get() = database.noteQueries

  private fun repository() = RealNoteRepository(
      noteQueries = noteQueries,
      ioScheduler = trampolineScheduler,
      clock = clock
  )

  @Test fun `insert a note correctly`() {
    val noteId = NoteId.generate()
    val content = "Nicolas Cage is a national treasure"
    repository().create(id = noteId, content = content).test()

    val savedNote = noteQueries.visibleNotes().executeAsOne()

    savedNote.let {
      assertThat(it.id).isEqualTo(noteId)
      assertThat(it.content).isEqualTo(content)
      assertThat(it.createdAt).isEqualTo(clock.nowUtc())
      assertThat(it.updatedAt).isEqualTo(clock.nowUtc())
      assertThat(it.isPendingDeletion).isFalse()
    }
  }

  @Test fun `update a note only if its content is changed`() {
    val note = fakeNote(id = NoteId.generate(), content = "# Nicolas", clock = clock)
    noteQueries.testInsert(note)

    repository().update(note.id, content = "# Nicolas").test()

    val savedNote = { noteQueries.note(note.id).executeAsOne() }
    assertThat(savedNote().updatedAt).isEqualTo(note.updatedAt)

    clock.advanceTimeBy(5.seconds)
    repository().update(note.id, content = "# Nicolas Cage").test()
    assertThat(savedNote().updatedAt).isEqualTo(note.updatedAt + 5.seconds)
  }

  @Test fun `mark a note as pending deletion`() {
    val note = fakeNote(id = NoteId.generate(), content = "# Nicolas Cage")
    noteQueries.testInsert(note)

    repository().markAsPendingDeletion(note.id).test()

    val savedNote = noteQueries.note(note.id).executeAsOne()
    assertThat(savedNote.isPendingDeletion).isTrue()
  }

  @Test fun `mark a note as archived`() {
    val note = fakeNote(id = NoteId.generate(), content = "# A national treasure")
    noteQueries.testInsert(note)

    repository().markAsArchived(note.id).test()

    val savedNote = noteQueries.note(note.id).executeAsOne()
    assertThat(savedNote.isArchived).isTrue()
  }
}
