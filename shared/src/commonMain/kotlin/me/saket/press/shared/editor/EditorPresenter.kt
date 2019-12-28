package me.saket.press.shared.editor

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.andThen
import com.badoo.reaktive.completable.completableOfEmpty
import com.badoo.reaktive.completable.subscribe
import com.badoo.reaktive.completable.subscribeOn
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOfEmpty
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.share
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.observable.withLatestFrom
import com.badoo.reaktive.scheduler.Scheduler
import me.saket.press.data.shared.Note
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorUiEffect.CloseNote
import me.saket.press.shared.editor.EditorUiEffect.UpdateNoteText
import me.saket.press.shared.localization.Strings.Editor
import me.saket.press.shared.note.NoteRepository
import me.saket.press.shared.note.deletedAt
import me.saket.press.shared.rx.mapToOptional
import me.saket.press.shared.rx.mapToSome
import me.saket.press.shared.rx.observableInterval
import me.saket.press.shared.ui.Presenter
import me.saket.press.shared.util.Optional
import me.saket.wysiwyg.formatting.TextSelection
import me.saket.wysiwyg.formatting.TextSelection.Companion

class EditorPresenter(
  args: Args,
  private val noteRepository: NoteRepository,
  private val ioScheduler: Scheduler,
  private val computationScheduler: Scheduler,
  private val strings: Editor,
  private val config: EditorConfig
) : Presenter<EditorEvent, EditorUiModel, EditorUiEffect> {

  private val openMode = args.openMode

  // replayingShare() would have been better.
  private val noteStream = createOrFetchNote().share()

  override fun uiModels(publishedEvents: Observable<EditorEvent>): Observable<EditorUiModel> {
    return publishedEvents.publish { sharedEvents ->
      val uiModels = sharedEvents
          .toggleHintText()
          .map { (hint) -> EditorUiModel(hintText = hint) }

      val autoSave = sharedEvents.autoSaveContent()

      merge(uiModels, autoSave)
    }
  }

  override fun uiEffects(publishedEvents: Observable<EditorEvent>): Observable<EditorUiEffect> {
    return merge(
        populateExistingNoteOnStart(),
        closeIfNoteGetsDeleted()
    )
  }

  private fun createOrFetchNote(): Observable<Note> {
    val newOrExistingId = when (openMode) {
      is NewNote -> openMode.placeholderUuid
      is ExistingNote -> openMode.noteUuid
    }

    // This function can get called multiple times. Don't create a
    // new note everytime. If the note was deleted on another device,
    // continue updating it.
    val createIfNeeded = noteRepository
        .note(newOrExistingId)
        .take(1)
        .flatMapCompletable { (existingNote) ->
          when (existingNote) {
            null -> noteRepository.create(newOrExistingId, NEW_NOTE_PLACEHOLDER)
            else -> completableOfEmpty()
          }
        }

    return createIfNeeded
        .andThen(noteRepository.note(newOrExistingId))
        .mapToSome()
  }

  private fun populateExistingNoteOnStart(): Observable<EditorUiEffect> {
    return noteStream
        .take(1)
        .map {
          val isNewNote = it.content == NEW_NOTE_PLACEHOLDER
          UpdateNoteText(
              newText = it.content,
              newSelection = if (isNewNote) TextSelection.cursor(it.content.length) else null
          )
        }
  }

  /**
   * Can happen if the note was deleted outside of the app (e.g., on another device).
   */
  private fun closeIfNoteGetsDeleted(): Observable<EditorUiEffect> {
    return noteStream
        .filter { it.deletedAt != null }
        .take(1)
        .map { CloseNote }
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

  private fun Observable<EditorEvent>.autoSaveContent(): Observable<EditorUiModel> {
    val textChanges = ofType<NoteTextChanged>().map { it.text }

    return noteStream
        .take(1)
        .flatMapCompletable { note ->
          observableInterval(config.autoSaveEvery, computationScheduler)
              .withLatestFrom(textChanges) { _, text -> text }
              .distinctUntilChanged()
              .flatMapCompletable { text -> noteRepository.update(note.uuid, text) }
        }
        .andThen(observableOfEmpty())
  }

  fun saveEditorContentOnExit(content: CharSequence) {
    updateOrDeleteNote(content.toString())
        .subscribeOn(ioScheduler)
        .subscribe()
  }

  private fun updateOrDeleteNote(content: String): Completable {
    val trimmedContent = content.trim()
    val shouldDelete = content.isBlank() || trimmedContent == NEW_NOTE_PLACEHOLDER.trim()

    val noteId = when (openMode) {
      is NewNote -> openMode.placeholderUuid
      is ExistingNote -> openMode.noteUuid
    }

    // For reasons I don't understand, noteStream doesn't get re-subscribed
    // when this function is called after EditorView gets detached. Fetching
    // the note again here.
    return noteRepository.note(noteId)
        .take(1)
        .mapToSome()
        .flatMapCompletable { note ->
          when {
            shouldDelete -> noteRepository.markAsDeleted(note.uuid)
            else -> noteRepository.update(note.uuid, content)
          }
        }
  }

  interface Factory {
    fun create(args: Args): EditorPresenter
  }

  data class Args(val openMode: EditorOpenMode)

  companion object {
    internal const val NEW_NOTE_PLACEHOLDER = "# "
  }
}
