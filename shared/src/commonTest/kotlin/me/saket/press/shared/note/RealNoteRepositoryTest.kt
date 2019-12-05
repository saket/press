package me.saket.press.shared.note

import ch.tutteli.atrium.api.fluent.en_GB.hasSize
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.badoo.reaktive.observable.firstOrError
import com.badoo.reaktive.scheduler.trampolineScheduler
import com.badoo.reaktive.single.blockingGet
import com.badoo.reaktive.test.completable.test
import com.benasher44.uuid.uuid4
import com.soywiz.klock.seconds
import me.saket.press.shared.AndroidJUnit4
import me.saket.press.shared.RunWith
import me.saket.press.shared.db.TestDatabase
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.time.FakeClock
import me.saket.press.shared.util.filterSome
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class RealNoteRepositoryTest {

  private val noteQueries = TestDatabase().noteQueries
  private val clock = FakeClock()

  private val repository = RealNoteRepository(
      noteQueries = noteQueries,
      ioScheduler = trampolineScheduler,
      clock = clock
  )

  @Test fun `create inserts note correctly`() {
    val noteUuid = uuid4()
    val content = "Nicolas Cage is a national treasure"
    noteQueries.testInsert(fakeNote(uuid = noteUuid, content = content))

    val savedNote = repository.note(noteUuid)
        .filterSome()
        .firstOrError()
        .blockingGet()

    savedNote.let {
      expect(it.uuid).toBe(noteUuid)
      expect(it.content).toBe(content)
      expect(it.createdAt).toBe(clock.nowUtc())
      expect(it.updatedAt).toBe(clock.nowUtc())
      expect(it.deletedAt).toBe(null)
    }
  }

  @Test fun `filter out empty notes if requested`() {
    noteQueries.testInsert(fakeNote(uuid = uuid4(), content = "# Non-empty note"))
    noteQueries.testInsert(fakeNote(uuid = uuid4(), content = ""))

    val savedNotes = repository.notes(includeEmptyNotes = false)
        .firstOrError()
        .blockingGet()

    expect(savedNotes).hasSize(1)
  }

  @Test fun `include empty notes if requested`() {
    noteQueries.testInsert(fakeNote(uuid = uuid4(), content = "# Non-empty note"))
    noteQueries.testInsert(fakeNote(uuid = uuid4(), content = ""))

    val savedNotes = repository.notes(includeEmptyNotes = true)
        .firstOrError()
        .blockingGet()

    expect(savedNotes).hasSize(2)
  }

  @Test fun `update a note only if its content is changed`() {
    val note = fakeNote(uuid = uuid4(), content = "# Nicolas")
    noteQueries.testInsert(note)
    val savedNote = { noteQueries.note(note.uuid).executeAsOne() }

    repository.update(note.uuid, content = "# Nicolas").test()
    expect(savedNote().updatedAt).toBe(note.updatedAt)

    clock.advanceTimeBy(5.seconds)
    repository.update(note.uuid, content = "# Nicolas Cage").test()
    expect(savedNote().updatedAt).toBe(note.updatedAt + 5.seconds)
  }
}
