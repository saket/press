package me.saket.press.shared.home

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.ofType
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.navigation.Navigator
import me.saket.press.shared.navigation.ScreenKey
import me.saket.press.shared.note.NoteRepository
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.ui.Presenter

class HomePresenter(
  private val args: Args,
  private val repository: NoteRepository
) : Presenter<HomeEvent, HomeUiModel, Nothing> {

  override fun uiModels(events: Observable<HomeEvent>): Observable<HomeUiModel> {
    return merge(populateNotes(), events.openNewNoteScreen())
  }

  private fun Observable<HomeEvent>.openNewNoteScreen(): Observable<HomeUiModel> =
    ofType<NewNoteClicked>()
        .consumeOnNext {
          args.navigator.goTo(ScreenKey.ComposeNewNote)
        }

  private fun populateNotes(): Observable<HomeUiModel> =
    repository.notes(args.includeEmptyNotes).map {
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
    fun create(args: Args): HomePresenter
  }

  /**
   * Adding or removing arguments to [HomePresenter] is a lot of effort because multiple
   * DI graphs are involved. Using Dagger with AssistedInjection everywhere would have
   * been nice.
   */
  data class Args(
    val navigator: Navigator,
    /**
     * [EditorPresenter] creates a new note as soon as the editor screen is opened,
     * causing the new note to show up on home while the new note screen is opening.
     * This flag ensures that doesn't happen by ignoring empty notes when its set to
     * false. In the future, this can be set to true for multi-pane layouts on desktop
     * or tablets.
     */
    val includeEmptyNotes: Boolean
  )
}
