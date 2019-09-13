package me.saket.compose.shared.editor

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableOfEmpty
import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.completable.subscribeOn
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.firstOrError
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.observable.observableOfEmpty
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.single.flatMapCompletable
import me.saket.compose.shared.Presenter
import me.saket.compose.shared.editor.EditorOpenMode.ExistingNote
import me.saket.compose.shared.editor.EditorUiModel.TransientUpdate
import me.saket.compose.shared.editor.EditorUiModel.TransientUpdate.CloseNote
import me.saket.compose.shared.editor.EditorUiModel.TransientUpdate.PopulateContent
import me.saket.compose.shared.note.NoteRepository
import me.saket.compose.shared.rx.take
import me.saket.compose.shared.util.filterNone
import me.saket.compose.shared.util.filterSome

class EditorPresenter(
  private val openMode: EditorOpenMode,
  private val noteRepository: NoteRepository,
  private val ioScheduler: Scheduler
) : Presenter<EditorEvent, EditorUiModel> {

  override fun contentModels(events: Observable<EditorEvent>): Observable<EditorUiModel> {
    val transientUpdates = merge(
        populateNoteOnStart(),
        closeIfNoteGetsDeleted()
    )
    return observableOf(EditorUiModel(transientUpdates))
  }

  private fun populateNoteOnStart(): Observable<TransientUpdate> {
    return if (openMode is ExistingNote) {
      noteRepository.note(openMode.noteUuid)
          .filterSome()
          .take(1)
          .map { PopulateContent(it.content) }
    } else {
      observableOfEmpty()
    }
  }

  /**
   * Can happen if the note was deleted outside of the app (e.g., on another device).
   */
  private fun closeIfNoteGetsDeleted(): Observable<TransientUpdate> {
    return if (openMode is ExistingNote) {
      noteRepository.note(openMode.noteUuid)
          .filterNone()
          .take(1)
          .map { CloseNote }

    } else {
      observableOfEmpty()
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

//  private fun Observable<EditorEvent>.toggleHintText(): Observable<Optional<String>> {
//    val textChanges = ofType<NoteTextChanged>().map { it.text }
//
//    val textIsPlaceholder = textChanges.filter { it == NEW_NOTE_PLACEHOLDER }
//    val textIsNotPlaceholder = textChanges.filter { it != NEW_NOTE_PLACEHOLDER }
//  }

  interface Factory {
    fun create(openMode: EditorOpenMode): EditorPresenter
  }

  companion object {
    const val NEW_NOTE_PLACEHOLDER = "# "
  }
}
