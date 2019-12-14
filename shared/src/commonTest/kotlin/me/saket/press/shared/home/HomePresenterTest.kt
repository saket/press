package me.saket.press.shared.home

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.badoo.reaktive.subject.publish.publishSubject
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.test
import com.badoo.reaktive.test.scheduler.TestScheduler
import com.badoo.reaktive.utils.isFrozen
import com.benasher44.uuid.uuid4
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.home.HomeUiEffect.ComposeNewNote
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.home.HomePresenter.Args
import me.saket.press.shared.note.FakeNoteRepository
import kotlin.test.Test
import kotlin.test.assertFalse

class HomePresenterTest {

  private val noteRepository = FakeNoteRepository()
  private val events = publishSubject<HomeEvent>()

  private val presenter = HomePresenter(
      args = Args(includeEmptyNotes = true),
      mainScheduler = TestScheduler(),
      repository = noteRepository
  )

  @Test fun `populate notes on creation`() {
    val noteUuid = uuid4()
    noteRepository.savedNotes += listOf(
        fakeNote(
            uuid = noteUuid,
            localId = -1L,
            content = "# Nicolas Cage\nOur national treasure"
        )
    )

    val testObserver = presenter.uiModels(events).test(autoFreeze = false)

    val noteUiModels = listOf(
        HomeUiModel.Note(
            noteUuid = noteUuid,
            adapterId = -1L,
            title = "Nicolas Cage",
            body = "Our national treasure"
        )
    )

    val uiModel = testObserver.values[0]
    assertThat(uiModel.notes).isEqualTo(noteUiModels)
    assertFalse(testObserver.isFrozen)
  }

  @Test fun `open new note screen when new note is clicked`() {
    presenter.uiEffects(events)
        .test(autoFreeze = false)
        .also { events.onNext(NewNoteClicked) }
        .apply {
          assertValue(ComposeNewNote)
          assertFalse(isFrozen)
        }
  }
}
