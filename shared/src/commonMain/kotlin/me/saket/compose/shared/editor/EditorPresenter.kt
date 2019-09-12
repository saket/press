package me.saket.compose.shared.editor

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completableOfEmpty
import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.completable.subscribeOn
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.firstOrError
import com.badoo.reaktive.observable.observableOfEmpty
import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.single.flatMapCompletable
import com.benasher44.uuid.Uuid
import me.saket.compose.shared.Presenter
import me.saket.compose.shared.localization.Strings
import me.saket.compose.shared.note.NoteRepository

class EditorPresenter(
  private val noteUuid: Uuid,
  private val noteRepository: NoteRepository,
  private val ioScheduler: Scheduler,
  private val strings: Strings.Editor
) : Presenter<EditorEvent, EditorUiModel> {

  override fun contentModels(events: Observable<EditorEvent>): Observable<EditorUiModel> {
    return observableOfEmpty()
  }

  fun saveEditorContentOnExit(content: CharSequence) {
    createUpdateOrDeleteNote(content.toString())
        .subscribeOn(ioScheduler)
        .subscribe()
  }

  private fun createUpdateOrDeleteNote(content: String): Completable {
    return noteRepository.note(noteUuid)
        .firstOrError()
        .flatMapCompletable { (existingNote) ->
          val hasExistingNote = existingNote != null
          val nonBlankContent = content.isNotBlank() && content.trim() != NEW_NOTE_PLACEHOLDER.trim()

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
    fun create(noteUuid: Uuid): EditorPresenter
  }

  companion object {
    const val NEW_NOTE_PLACEHOLDER = "# "
  }
}