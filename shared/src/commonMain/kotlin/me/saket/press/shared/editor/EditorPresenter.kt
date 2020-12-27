package me.saket.press.shared.editor

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.andThen
import com.badoo.reaktive.completable.completableFromFunction
import com.badoo.reaktive.completable.completableOfEmpty
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableWrapper
import com.badoo.reaktive.observable.combineLatest
import com.badoo.reaktive.observable.distinctUntilChanged
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.flatMap
import com.badoo.reaktive.observable.flatMapCompletable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOfEmpty
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.publish
import com.badoo.reaktive.observable.refCount
import com.badoo.reaktive.observable.replay
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.observable.takeUntil
import com.badoo.reaktive.observable.withLatestFrom
import com.badoo.reaktive.observable.wrap
import me.saket.press.PressDatabase
import me.saket.press.data.shared.Note
import me.saket.press.shared.editor.EditorEvent.ArchiveToggleClicked
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorUiEffect.BlockedDueToSyncConflict
import me.saket.press.shared.editor.EditorUiEffect.UpdateNoteText
import me.saket.press.shared.home.HomePresenter
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.asObservable
import me.saket.press.shared.rx.combineLatestWith
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.rx.filterNotNull
import me.saket.press.shared.rx.filterNull
import me.saket.press.shared.rx.mapToOne
import me.saket.press.shared.rx.mapToOneOrNull
import me.saket.press.shared.rx.observableInterval
import me.saket.press.shared.sync.SyncMergeConflicts
import me.saket.press.shared.sync.git.FolderPaths
import me.saket.press.shared.time.Clock
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.Presenter
import me.saket.wysiwyg.formatting.TextSelection

class EditorPresenter(
  val args: Args,
  private val clock: Clock,
  private val database: PressDatabase,
  private val schedulers: Schedulers,
  private val strings: Strings,
  private val config: EditorConfig,
  private val syncConflicts: SyncMergeConflicts
) : Presenter<EditorEvent, EditorUiModel, EditorUiEffect>() {

  private val openMode = args.openMode
  private val noteQueries get() = database.noteQueries
  private val noteStream = createOrFetchNote().replay(1).refCount()
  private val folderPaths = FolderPaths(database)

  override fun defaultUiModel() =
    EditorUiModel(hintText = null, isArchived = false)

  override fun uiModels(): ObservableWrapper<EditorUiModel> {
    return viewEvents().publish { events ->
      val hintTexts = events.toggleHintText()
      val uiModels = combineLatest(hintTexts, isNoteArchived()) { hintText, isArchived ->
        EditorUiModel(
          hintText = hintText,
          isArchived = isArchived
        )
      }.distinctUntilChanged()

      return@publish merge(
        uiModels,
        events.autoSaveContent(),
        handleArchiveClicks(events)
      )
    }.wrap()
  }

  override fun uiEffects(): ObservableWrapper<EditorUiEffect> {
    return merge(
      populateExistingNoteOnStart(),
      blockEditingOnSyncConflict()
    ).wrap()
  }

  private fun createOrFetchNote(): Observable<Note> {
    val newOrExistingId = when (val it = openMode.noteId) {
      is PlaceholderNoteId -> it.id
      is PreSavedNoteId -> it.id
    }

    val createIfNeeded = if (openMode is NewNote) {
      // This function can get called multiple times if it's re-subscribed.
      // Create a new note only if one doesn't exist already.
      noteQueries.note(newOrExistingId)
        .asObservable(schedulers.io)
        .mapToOneOrNull()
        .take(1)
        .filterNull()
        .flatMapCompletable {
          completableFromFunction {
            noteQueries.insert(
              id = newOrExistingId,
              folderId = null,
              content = openMode.preFilledNote.ifBlankOrNull { NEW_NOTE_PLACEHOLDER },
              createdAt = clock.nowUtc(),
              updatedAt = clock.nowUtc()
            )
          }
        }
    } else {
      // If the note gets deleted on another device (that is, deletedAt != null),
      // Press will continue updating the same note.
      completableOfEmpty()
    }

    return createIfNeeded.andThen(
      noteQueries.note(newOrExistingId)
        .asObservable(schedulers.io)
        .mapToOne()
    )
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

  private fun blockEditingOnSyncConflict(): Observable<EditorUiEffect> {
    return noteConflicts().map { BlockedDueToSyncConflict }
  }

  private fun noteConflicts(): Observable<Unit> {
    return noteStream
      .take(1)
      .flatMap { syncConflicts.isConflicted(it.id) }
      .filter { it }
      .map { Unit }
  }

  private fun Observable<EditorEvent>.toggleHintText(): Observable<String?> {
    val randomHint = strings.editor.new_note_hints.shuffled().first()

    return ofType<NoteTextChanged>()
      .distinctUntilChanged()
      .map { (text) ->
        when {
          text.trimEnd() == NEW_NOTE_PLACEHOLDER.trim() -> "# $randomHint"
          else -> null
        }
      }
  }

  private fun isNoteArchived(): Observable<Boolean> {
    return noteStream
      .map { it.folderId }
      .distinctUntilChanged()
      .map(folderPaths::isArchived)
  }

  private fun handleArchiveClicks(
    events: Observable<EditorEvent>
  ): Observable<EditorUiModel> {
    return events.ofType<ArchiveToggleClicked>()
      .withLatestFrom(noteStream, ::Pair)
      .observeOn(schedulers.io)
      .consumeOnNext { (event, note) ->
        folderPaths.setArchived(note.id, archive = event.archive)

        if (event.archive) {
          args.navigator.goBack()
        }
      }
  }

  private fun Observable<EditorEvent>.autoSaveContent(): Observable<EditorUiModel> {
    val textChanges = ofType<NoteTextChanged>().map { it.text }

    return noteStream
      .take(1)
      .flatMapCompletable { note ->
        observableInterval(config.autoSaveEvery, schedulers.computation)
          .withLatestFrom(textChanges) { _, text -> text }
          .distinctUntilChanged()
          .takeUntil(noteConflicts())
          .flatMapCompletable { text ->
            completableFromFunction {
              noteQueries.updateContent(
                id = note.id,
                content = text,
                updatedAt = clock.nowUtc()
              )
            }
          }
      }
      .andThen(observableOfEmpty())
  }

  internal fun saveEditorContentOnClose(content: String): Completable {
    val shouldDelete = openMode is NewNote
      && args.deleteBlankNewNoteOnExit
      && content.trim().let { it.isBlank() || it == NEW_NOTE_PLACEHOLDER.trim() }

    val noteId = when (val it = openMode.noteId) {
      is PlaceholderNoteId -> it.id
      is PreSavedNoteId -> it.id
    }

    // For reasons I don't understand, noteStream doesn't get re-subscribed
    // when this function is called after EditorView gets detached. Fetching
    // the note again here.
    return noteQueries.note(noteId)
      .asObservable(schedulers.io)
      .mapToOneOrNull()
      .filterNotNull()
      .combineLatestWith(syncConflicts.isConflicted(noteId))
      .filter { (_, isConflicted) -> !isConflicted }
      .take(1)
      .flatMapCompletable { (note) ->
        val maybeDelete = when {
          shouldDelete -> completableFromFunction { noteQueries.markAsPendingDeletion(note.id) }
          else -> completableOfEmpty()
        }
        val update = completableFromFunction {
          noteQueries.updateContent(
            id = note.id,
            content = content,
            updatedAt = clock.nowUtc()
          )
        }
        return@flatMapCompletable update.andThen(maybeDelete)
      }
  }

  fun interface Factory {
    fun create(args: Args): EditorPresenter
  }

  data class Args(
    val openMode: EditorOpenMode,
    /** Should be kept in sync with [HomePresenter.Args.includeBlankNotes]. */
    val deleteBlankNewNoteOnExit: Boolean,
    val navigator: Navigator
  )

  companion object {
    const val NEW_NOTE_PLACEHOLDER = "# "
  }
}

private fun String?.ifBlankOrNull(default: () -> String): String {
  return this?.ifBlank(default) ?: default()
}
