package me.saket.press.shared.note

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.badoo.reaktive.observable.firstOrError
import com.badoo.reaktive.scheduler.trampolineScheduler
import com.badoo.reaktive.single.blockingGet
import com.badoo.reaktive.test.completable.test
import com.benasher44.uuid.uuid4
import com.soywiz.klock.seconds
import me.saket.press.shared.AndroidJUnit4
import me.saket.press.shared.RunWith
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.time.FakeClock
import me.saket.press.shared.util.filterSome
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class RealNoteRepositoryTest : BaseDatabaeTest() {

  private val clock = FakeClock()
  private val noteQueries get() = database().noteQueries

  private fun repository() = RealNoteRepository(
      noteQueries = noteQueries,
      ioScheduler = trampolineScheduler,
      clock = clock
  )

  @Test fun `create inserts note correctly`() {
    val noteUuid = uuid4()
    val content = "Nicolas Cage is a national treasure"
    noteQueries.testInsert(fakeNote(uuid = noteUuid, content = content))

    val savedNote = repository().note(noteUuid)
        .filterSome()
        .firstOrError()
        .blockingGet()

    savedNote.let {
      assertThat(it.uuid).isEqualTo(noteUuid)
      assertThat(it.content).isEqualTo(content)
      assertThat(it.createdAt).isEqualTo(clock.nowUtc())
      assertThat(it.updatedAt).isEqualTo(clock.nowUtc())
      assertThat(it.deletedAt).isEqualTo(null)
    }
  }

  @Test fun `update a note only if its content is changed`() {
    val note = fakeNote(uuid = uuid4(), content = "# Nicolas")
    noteQueries.testInsert(note)
    val savedNote = { noteQueries.note(note.uuid).executeAsOne() }

    repository().update(note.uuid, content = "# Nicolas").test()
    assertThat(savedNote().updatedAt).isEqualTo(note.updatedAt)

    clock.advanceTimeBy(5.seconds)
    repository().update(note.uuid, content = "# Nicolas Cage").test()
    assertThat(savedNote().updatedAt).isEqualTo(note.updatedAt + 5.seconds)
  }

  @Test fun `mark a note as deleted`() {
    val note = fakeNote(uuid = uuid4(), content = "# Nicolas Cage")
    noteQueries.testInsert(note)

    repository().markAsDeleted(note.uuid).test()

    val savedNote = noteQueries.note(note.uuid).executeAsOne()
    assertThat(savedNote.deletedAt).isNotNull()
  }

  @Test fun `mark a note as archived`() {
    val note = fakeNote(uuid = uuid4(), content = "Jake Wharton secretly loves Flutter")
    noteQueries.testInsert(note)

    repository().markAsArchived(note.uuid).test()

    val savedNote = noteQueries.note(note.uuid).executeAsOne()
    assertThat(savedNote.archivedAt).isNotNull()
  }
}
