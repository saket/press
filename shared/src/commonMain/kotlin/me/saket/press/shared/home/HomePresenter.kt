package me.saket.press.shared.home

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.ofType
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.navigation.Navigator
import me.saket.press.shared.navigation.ScreenKey
import me.saket.press.shared.note.NoteRepository
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.ui.Presenter

class HomePresenter(
  private val repository: NoteRepository,
  private val navigator: Navigator,
  private val includeEmptyNotes: Boolean
) : Presenter<HomeEvent, HomeUiModel, Nothing> {

  override fun uiModels(events: Observable<HomeEvent>): Observable<HomeUiModel> {
    return merge(populateNotes(), events.openNewNoteScreen())
  }

  private fun Observable<HomeEvent>.openNewNoteScreen(): Observable<HomeUiModel> =
    ofType<NewNoteClicked>()
        .consumeOnNext {
          navigator.goTo(ScreenKey.ComposeNewNote)
        }

  private fun populateNotes(): Observable<HomeUiModel> =
    repository.notes(includeEmptyNotes).map {
      HomeUiModel(it.map { note ->
        val (heading, body) = SplitHeadingAndBody.split(note.content)

        HomeUiModel.Note(
            noteUuid = note.uuid,
            adapterId = note.localId,
            title = heading,
            body = body
        )
      })
    }

  interface Factory {
    fun create(
      navigator: Navigator,
      includeEmptyNotes: Boolean
    ): HomePresenter
  }
}
