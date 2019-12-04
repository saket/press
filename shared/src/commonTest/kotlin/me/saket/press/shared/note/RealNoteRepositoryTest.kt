package me.saket.press.shared.note

import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.observable.firstOrError
import com.badoo.reaktive.scheduler.trampolineScheduler
import com.badoo.reaktive.single.blockingGet
import com.badoo.reaktive.test.completable.assertComplete
import com.badoo.reaktive.test.completable.test
import com.benasher44.uuid.uuid4
import me.saket.press.shared.AndroidJUnit4
import me.saket.press.shared.RunWith
import me.saket.press.shared.db.TestDatabase
import me.saket.press.shared.time.FakeClock
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldHaveSize
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

    repository.create(noteUuid, content)
        .test()
        .assertComplete()

    val (savedNote) = repository.note(noteUuid)
        .firstOrError()
        .blockingGet()

    check(savedNote != null)
    savedNote.uuid shouldEqual noteUuid
    savedNote.content shouldEqual content
    savedNote.createdAt shouldEqual clock.nowUtc()
    savedNote.updatedAt shouldEqual clock.nowUtc()
    savedNote.deletedAt shouldEqual null
  }

  @Test fun `filter out empty notes if requested`() {
    repository.create(
        InsertNote(uuid4(), "# Non-empty note"),
        InsertNote(uuid4(), "")
    ).subscribe()

    val savedNotes = repository.notes(includeEmptyNotes = false)
        .firstOrError()
        .blockingGet()

    savedNotes shouldHaveSize 1
  }

  @Test fun `include empty notes if requested`() {
    repository.create(
        InsertNote(uuid4(), "# Non-empty note"),
        InsertNote(uuid4(), "")
    ).subscribe()

    val savedNotes = repository.notes(includeEmptyNotes = true)
        .firstOrError()
        .blockingGet()

    savedNotes shouldHaveSize 2
  }
}
