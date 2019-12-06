package me.saket.press.shared.home

import com.badoo.reaktive.subject.publish.publishSubject
import com.badoo.reaktive.test.observable.test
import com.benasher44.uuid.uuid4
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.home.HomePresenter.Args
import me.saket.press.shared.navigation.FakeNavigator
import me.saket.press.shared.navigation.ScreenKey
import me.saket.press.shared.note.FakeNoteRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class HomePresenterTest {

  private val noteRepository = FakeNoteRepository()
  private val navigator = FakeNavigator()

  private val presenter = HomePresenter(
      args = Args(navigator, includeEmptyNotes = true),
      repository = noteRepository
  )
  private val events = publishSubject<HomeEvent>()

  @Test fun `populate notes on creation`() {
    val noteUuid = uuid4()
    noteRepository.savedNotes += listOf(fakeNote(
        uuid = noteUuid,
        localId = -1L,
        content = "# Nicolas Cage\nOur national treasure"
    ))

    val testObserver = presenter.uiModels(events).test()

    val noteUiModels = listOf(HomeUiModel.Note(
        noteUuid = noteUuid,
        adapterId = -1L,
        title = "Nicolas Cage",
        body = "Our national treasure"
    ))

    val uiModel = testObserver.values[0]
    assertEquals(noteUiModels, uiModel.notes)
  }

  @Test fun `open new note screen when new note is clicked`() {
    presenter.uiModels(events).test()

    events.onNext(NewNoteClicked)

    assertSame(ScreenKey.ComposeNewNote, navigator.backstack.last())
  }
}
