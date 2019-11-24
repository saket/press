package me.saket.press.shared.editor

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableOfEmpty
import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.completable.subscribeOn
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.firstOrError
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.observable.observableOfEmpty
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.single.flatMapCompletable
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorUiUpdate.CloseNote
import me.saket.press.shared.editor.EditorUiUpdate.PopulateContent
import me.saket.press.shared.localization.Strings.Editor
import me.saket.press.shared.note.NoteRepository
import me.saket.press.shared.rx.mapToOptional
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.util.Optional
import me.saket.press.shared.util.filterNone
import me.saket.press.shared.util.filterSome

class EditorPresenter(
  private val openMode: EditorOpenMode,
  private val noteRepository: NoteRepository,
  private val ioScheduler: Scheduler,
  private val strings: Editor
) : Presenter<EditorEvent, EditorUiModel, EditorUiUpdate> {

  override fun uiModels(events: Observable<EditorEvent>): Observable<EditorUiModel> {
    return events
        .toggleHintText()
        .map { (hint) -> EditorUiModel(hintText = hint) }
  }

  override fun uiUpdates(): Observable<EditorUiUpdate> {
    return merge(
        populateExistingNoteOnStart(),
        populateNewNotePlaceholderOnStart(),
        closeIfNoteGetsDeleted()
    )
  }

  private fun populateExistingNoteOnStart(): Observable<EditorUiUpdate> {
    return if (openMode is ExistingNote) {
      noteRepository.note(openMode.noteUuid)
          .filterSome()
          .take(1)
          .map { PopulateContent(it.content) }
    } else {
      observableOfEmpty()
    }
  }

  private fun populateNewNotePlaceholderOnStart(): Observable<EditorUiUpdate> {
    return if (openMode is NewNote) {
      observableOf(PopulateContent(NEW_NOTE_PLACEHOLDER))
    } else {
      observableOfEmpty()
    }
  }

  /**
   * Can happen if the note was deleted outside of the app (e.g., on another device).
   */
  private fun closeIfNoteGetsDeleted(): Observable<EditorUiUpdate> {
    return if (openMode is ExistingNote) {
      noteRepository.note(openMode.noteUuid)
          .filterNone()
          .take(1)
          .map { CloseNote }

    } else {
      observableOfEmpty()
    }
  }

  private fun Observable<EditorEvent>.toggleHintText(): Observable<Optional<String>> {
    val randomHint = strings.newNoteHints.shuffled().first()

    return ofType<NoteTextChanged>()
        .distinctUntilChanged()
        .mapToOptional { (text) ->
          when {
            text.trimEnd() == NEW_NOTE_PLACEHOLDER.trim() -> randomHint
            else -> null
          }
        }
  }

  fun saveEditorContentOnExit(content: CharSequence) {
    createUpdateOrDeleteNote(content.toString())
        .subscribeOn(ioScheduler)
        .subscribe()
  }

  private fun createUpdateOrDeleteNote(content: String): Completable {
    val noteUuid = openMode.noteUuid
    return noteRepository.note(noteUuid)
        .firstOrError()
        .flatMapCompletable { (existingNote) ->
          val hasExistingNote = existingNote != null
          val nonBlankContent =
            content.isNotBlank() && content.trim() != NEW_NOTE_PLACEHOLDER.trim()

          val shouldCreate = hasExistingNote.not() && nonBlankContent
          val shouldUpdate = hasExistingNote && nonBlankContent
          val shouldDelete = hasExistingNote && nonBlankContent.not()

          when {
            shouldCreate -> noteRepository.create(noteUuid, content)
            shouldUpdate -> noteRepository.update(noteUuid, content)
            shouldDelete -> noteRepository.markAsDeleted(noteUuid)
            else -> completableOfEmpty()
          }
        }
  }

  interface Factory {
    fun create(openMode: EditorOpenMode): EditorPresenter
  }

  companion object {
    internal const val NEW_NOTE_PLACEHOLDER = "# "
  }
}
