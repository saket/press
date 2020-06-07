package me.saket.press.shared.note

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
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

    val savedNote = noteQueries.notes().executeAsOne()

    savedNote.let {
      assertThat(it.uuid).isEqualTo(noteId)
      assertThat(it.content).isEqualTo(content)
      assertThat(it.createdAt).isEqualTo(clock.nowUtc())
      assertThat(it.updatedAt).isEqualTo(clock.nowUtc())
      assertThat(it.deletedAt).isEqualTo(null)
    }
  }

  @Test fun `update a note only if its content is changed`() {
    val note = fakeNote(noteId = NoteId.generate(), content = "# Nicolas", clock = clock)
    noteQueries.testInsert(note)

    repository().update(note.uuid, content = "# Nicolas").test()

    val savedNote = { noteQueries.note(note.uuid).executeAsOne() }
    assertThat(savedNote().updatedAt).isEqualTo(note.updatedAt)

    clock.advanceTimeBy(5.seconds)
    repository().update(note.uuid, content = "# Nicolas Cage").test()
    assertThat(savedNote().updatedAt).isEqualTo(note.updatedAt + 5.seconds)
  }

  @Test fun `mark a note as deleted`() {
    val note = fakeNote(noteId = NoteId.generate(), content = "# Nicolas Cage")
    noteQueries.testInsert(note)

    repository().markAsDeleted(note.uuid).test()

    val savedNote = noteQueries.note(note.uuid).executeAsOne()
    assertThat(savedNote.deletedAt).isNotNull()
  }

  @Test fun `mark a note as archived`() {
    val note = fakeNote(noteId = NoteId.generate(), content = "# A national treasure")
    noteQueries.testInsert(note)

    repository().markAsArchived(note.uuid).test()

    val savedNote = noteQueries.note(note.uuid).executeAsOne()
    assertThat(savedNote.archivedAt).isNotNull()
  }
}
