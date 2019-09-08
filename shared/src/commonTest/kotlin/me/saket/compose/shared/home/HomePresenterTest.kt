package me.saket.compose.shared.home

import com.badoo.reaktive.subject.publish.publishSubject
import com.badoo.reaktive.test.observable.test
import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTimeTz
import me.saket.compose.data.shared.Note
import me.saket.compose.shared.home.HomeEvent.NewNoteClicked
import me.saket.compose.shared.navigation.FakeNavigator
import me.saket.compose.shared.navigation.ScreenKey
import me.saket.compose.shared.note.FakeNoteRepository
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

    val notes = listOf(Note.Impl(
        id = uuid4(),
        content = "Nicolas Cage",
        createdAt = DateTimeTz.nowLocal(),
        updatedAt = DateTimeTz.nowLocal(),
        deletedAt = null
    ))
    noteRepository.noteSubject.onNext(notes)

    val noteUiModels = listOf(HomeUiModel.Note(
        adapterId = 0L,
        content = "Nicolas Cage"
    ))

    val uiModel = testObserver.values[0]
    assertEquals(noteUiModels, uiModel.notes)
  }

  @Test fun `open new note screen when new note is clicked`() {
    presenter.contentModels(events).test()

    events.onNext(NewNoteClicked)

    assertSame(ScreenKey.NewNote, navigator.backstack.last())
  }
}