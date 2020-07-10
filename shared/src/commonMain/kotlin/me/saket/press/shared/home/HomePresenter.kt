package me.saket.press.shared.home

import com.badoo.reaktive.completable.andThen
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOfEmpty
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.wrap
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.keyboard.KeyboardShortcuts
import me.saket.press.shared.keyboard.KeyboardShortcuts.Companion.newNote
import me.saket.press.shared.note.NoteRepository
import me.saket.press.shared.rx.mergeWith
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.Presenter

class HomePresenter(
  private val args: Args,
  private val repository: NoteRepository,
  private val keyboardShortcuts: KeyboardShortcuts
) : Presenter<HomeEvent, HomeUiModel, Nothing>() {

  override fun defaultUiModel() =
    HomeUiModel(notes = emptyList())

  override fun uiModels() =
    merge(populateNotes(), openNewNoteScreen()).wrap()

  private fun openNewNoteScreen(): Observable<HomeUiModel> {
    return viewEvents().ofType<NewNoteClicked>()
        .mergeWith(keyboardShortcuts.listen(newNote))
        .flatMapCompletable {
          val newNoteId = NoteId.generate()
          repository
              .create(newNoteId, NEW_NOTE_PLACEHOLDER)
              .andThen(completableFromFunction {
                args.navigator.lfg(ComposeNewNote(newNoteId))
              })
        }
        .andThen(observableOfEmpty())
  }

  private fun populateNotes(): Observable<HomeUiModel> {
    val canInclude = { note: Note ->
      args.includeEmptyNotes || (note.content.isNotBlank() && note.content != NEW_NOTE_PLACEHOLDER)
    }

    return repository.visibleNotes()
        .map { notes -> notes.filter { canInclude(it) } }
        .map {
          HomeUiModel(it.map { note ->
            val (heading, body) = SplitHeadingAndBody.split(note.content)
            HomeUiModel.Note(
                noteId = note.id,
                adapterId = note.localId,
                title = heading,
                body = body
            )
          })
        }
  }

  interface Factory {
    fun create(args: Args): HomePresenter
  }

  data class Args(
    /**
     * [EditorPresenter] creates a new note as soon as the editor screen is opened,
     * causing the new note to show up on home while the new note screen is opening.
     * This flag ensures that doesn't happen by ignoring empty notes when its set to
     * false. In the future, this can be set to true for multi-pane layouts on desktop
     * or tablets.
     *
     * Should be kept in sync with [EditorPresenter.Args.archiveEmptyNoteOnExit].
     */
    val includeEmptyNotes: Boolean,

    val navigator: Navigator
  )
}
