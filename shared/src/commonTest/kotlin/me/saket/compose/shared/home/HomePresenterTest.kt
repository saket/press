package me.saket.compose.shared.home

import com.badoo.reaktive.subject.publish.publishSubject
import com.badoo.reaktive.test.observable.test
import me.saket.compose.shared.home.HomeEvent.NewNoteClicked
import me.saket.compose.shared.navigation.FakeNavigator
import me.saket.compose.shared.navigation.ScreenKey
import me.saket.compose.shared.note.FakeNoteRepository
import me.saket.compose.shared.note.Note
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class HomePresenterTest {

  private val noteRepository = FakeNoteRepository()
  private val navigator = FakeNavigator()

  private val presenter = HomePresenter(noteRepository, navigator)
  private val events = publishSubject<HomeEvent>()

  @Test fun `populate notes on creation`() {
    val testObserver = presenter.contentModels(events).test()

    val notes = listOf(
        Note(
            id = 0L,
            title = "Nicolas Cage",
            body = "Our national treasure"
        )
    )
    noteRepository.noteSubject.onNext(notes)

    val uiModel = testObserver.values[0]
    assertEquals(notes, uiModel.notes)
  }

  @Test fun `open new note screen when new note is clicked`() {
    presenter.contentModels(events).test()

    events.onNext(NewNoteClicked)

    assertSame(ScreenKey.NewNote, navigator.backstack.last())
  }
}