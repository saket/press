package me.saket.press.shared.note

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.badoo.reaktive.test.completable.test
import com.soywiz.klock.hours
import com.soywiz.klock.seconds
import me.saket.press.shared.FakeSchedulers
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.time.FakeClock
import kotlin.test.Test

class RealNoteRepositoryTest : BaseDatabaeTest() {
  private val clock = FakeClock()
  private val noteQueries get() = database.noteQueries

  private fun repository() = RealNoteRepository(
    database = database,
    schedulers = FakeSchedulers(),
    clock = clock
  )

  @Test fun `insert a note correctly`() {
    val noteId = NoteId.generate()
    val content = "Nicolas Cage is a national treasure"
    repository().create(id = noteId, content = content).test()

    val savedNote = noteQueries.allNotes().executeAsOne()

    savedNote.let {
      assertThat(it.id).isEqualTo(noteId)
      assertThat(it.content).isEqualTo(content)
      assertThat(it.createdAt).isEqualTo(clock.nowUtc())
      assertThat(it.updatedAt).isEqualTo(clock.nowUtc())
      assertThat(it.isPendingDeletion).isFalse()
    }
  }
}
