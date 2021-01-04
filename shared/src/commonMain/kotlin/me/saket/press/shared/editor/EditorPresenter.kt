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
import com.badoo.reaktive.observable.take
import com.badoo.reaktive.observable.takeUntil
import com.badoo.reaktive.observable.withLatestFrom
import com.badoo.reaktive.observable.wrap
import me.saket.press.PressDatabase
import me.saket.press.data.shared.Note
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorEvent.ArchiveToggleClicked
import me.saket.press.shared.editor.EditorEvent.CopyAsClicked
import me.saket.press.shared.editor.EditorEvent.DuplicateNoteClicked
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorEvent.ShareAsClicked
import me.saket.press.shared.editor.EditorEvent.SplitScreenClicked
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorUiEffect.BlockedDueToSyncConflict
import me.saket.press.shared.editor.EditorUiEffect.UpdateNoteText
import me.saket.press.shared.editor.TextFormat.Html
import me.saket.press.shared.editor.TextFormat.Markdown
import me.saket.press.shared.editor.TextFormat.RichText
import me.saket.press.shared.editor.ToolbarIconKind.Archive
import me.saket.press.shared.editor.ToolbarIconKind.CopyAs
import me.saket.press.shared.editor.ToolbarIconKind.DuplicateNote
import me.saket.press.shared.editor.ToolbarIconKind.OpenInSplitScreen
import me.saket.press.shared.editor.ToolbarIconKind.ShareAs
import me.saket.press.shared.editor.ToolbarIconKind.Unarchive
import me.saket.press.shared.home.HomePresenter
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.rx.Schedulers
import me.saket.press.shared.rx.asObservable
import me.saket.press.shared.rx.combineLatestWith
import me.saket.press.shared.rx.consumeOnNext
import me.saket.press.shared.rx.filterNotNull
import me.saket.press.shared.rx.filterNull
import me.saket.press.shared.rx.mapToOneOrNull
import me.saket.press.shared.rx.observableInterval
import me.saket.press.shared.rx.withLatestFrom
import me.saket.press.shared.syncer.SyncMergeConflicts
import me.saket.press.shared.syncer.git.DeviceInfo
import me.saket.press.shared.syncer.git.FolderPaths
import me.saket.press.shared.time.Clock
import me.saket.press.shared.ui.Clipboard
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.Presenter
import me.saket.wysiwyg.formatting.TextSelection
import me.saket.wysiwyg.parser.MarkdownParser

class EditorPresenter(
  val args: Args,
  private val clock: Clock,
  private val database: PressDatabase,
  private val schedulers: Schedulers,
  private val strings: Strings,
  private val config: EditorConfig,
  private val syncConflicts: SyncMergeConflicts,
  private val markdownParser: MarkdownParser,
  private val clipboard: Clipboard,
  private val deviceInfo: DeviceInfo
) : Presenter<EditorEvent, EditorUiModel>() {

  private val openMode = args.openMode
  private val noteQueries get() = database.noteQueries
  private val folderPaths = FolderPaths(database)
  private val intentLauncher get() = args.navigator.intentLauncher()

  override fun defaultUiModel() =
    EditorUiModel(
      hintText = null,
      toolbarMenu = emptyList()
    )

  override fun models(): ObservableWrapper<EditorUiModel> {
    return viewEvents().publish { events ->
      createOrFetchNote().publish { noteStream ->
        val models = combineLatest(
          events.toggleHintText(),
          buildToolbarMenu(noteStream),
          ::EditorUiModel
        )

        merge(
          models.distinctUntilChanged(),
          events.autoSaveContent(noteStream),
          handleArchiveClicks(events, noteStream),
          handleShareClicks(events),
          handleDuplicateNoteClicks(events, noteStream),
          handleSplitScreenClicks(events, noteStream),
          handleCopyClicks(events),
          populateExistingNoteOnStart(noteStream),
          blockEditingOnSyncConflict(noteStream)
        )
      }
    }.wrap()
  }

  private fun createOrFetchNote(): Observable<Note> {
    val newOrExistingId = when (val it = openMode.noteId) {
      is PlaceholderNoteId -> it.id
      is PreSavedNoteId -> it.id
    }

    val createIfNeeded = if (openMode is NewNote) {
      // This function can get called multiple times if it's re-subscribed.
      // Create a new note only if one doesn't exist already.
      noteQueries.note(newOrExistingId) // todo: use a single insert statement that ignores if the note already exists.
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
      completableOfEmpty()
    }

    return createIfNeeded.andThen(
      noteQueries.note(newOrExistingId)
        .asObservable(schedulers.io)
        .mapToOneOrNull()
        .filterNotNull()
    )
  }

  private fun populateExistingNoteOnStart(noteStream: Observable<Note>): Observable<EditorUiModel> {
    return noteStream
      .take(1)
      .consumeOnNext {
        val isNewNote = it.content == NEW_NOTE_PLACEHOLDER
        args.onEffect(
          UpdateNoteText(
            newText = it.content,
            newSelection = if (isNewNote) TextSelection.cursor(it.content.length) else null
          )
        )
      }
  }

  private fun blockEditingOnSyncConflict(noteStream: Observable<Note>): Observable<EditorUiModel> {
    return noteConflicts(noteStream).consumeOnNext {
      args.onEffect(BlockedDueToSyncConflict)
    }
  }

  private fun noteConflicts(noteStream: Observable<Note>): Observable<Unit> {
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

  private fun buildToolbarMenu(noteStream: Observable<Note>): Observable<List<ToolbarMenuItem>> {
    val isNoteArchived = noteStream.map { it.folderId }
      .distinctUntilChanged()
      .map(folderPaths::isArchived)
      .distinctUntilChanged()

    return isNoteArchived.map { isArchived ->
      listOfNotNull(
        if (isArchived) {
          ToolbarMenuAction(
            label = strings.editor.menu_unarchive,
            icon = Unarchive,
            clickEvent = ArchiveToggleClicked(archive = false)
          )
        } else {
          ToolbarMenuAction(
            label = strings.editor.menu_archive,
            icon = Archive,
            clickEvent = ArchiveToggleClicked(archive = true)
          )
        },
        ToolbarSubMenu(
          label = strings.editor.menu_share_as,
          icon = ShareAs,
          children = listOf(
            ToolbarMenuAction(
              label = strings.editor.menu_share_as_markdown,
              clickEvent = ShareAsClicked(format = Markdown)
            ),
            ToolbarMenuAction(
              label = strings.editor.menu_share_as_html,
              clickEvent = ShareAsClicked(format = Html)
            ),
            ToolbarMenuAction(
              label = strings.editor.menu_share_as_richtext,
              clickEvent = ShareAsClicked(format = RichText)
            )
          )
        ),
        ToolbarSubMenu(
          label = strings.editor.menu_copy_as,
          icon = CopyAs,
          children = listOf(
            ToolbarMenuAction(
              label = strings.editor.menu_copy_as_markdown,
              clickEvent = CopyAsClicked(format = Markdown)
            ),
            ToolbarMenuAction(
              label = strings.editor.menu_copy_as_html,
              clickEvent = CopyAsClicked(format = Html)
            ),
            ToolbarMenuAction(
              label = strings.editor.menu_copy_as_richtext,
              clickEvent = CopyAsClicked(format = RichText)
            )
          )
        ),
        ToolbarMenuAction(
          label = strings.editor.menu_duplicate_note,
          icon = DuplicateNote,
          clickEvent = DuplicateNoteClicked
        ),
        if (deviceInfo.supportsSplitScreen()) {
          ToolbarMenuAction(
            label = strings.editor.menu_open_in_split_screen,
            icon = OpenInSplitScreen,
            clickEvent = SplitScreenClicked
          )
        } else null,
      )
    }
  }

  private fun handleArchiveClicks(
    events: Observable<EditorEvent>,
    noteStream: Observable<Note>
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

  private fun handleCopyClicks(
    events: Observable<EditorEvent>
  ): Observable<EditorUiModel> {
    val copyClicks = events.ofType<CopyAsClicked>().map { it.format }
    val noteChanges = events.ofType<NoteTextChanged>().map { it.text }

    return copyClicks.withLatestFrom(noteChanges)
      .observeOn(schedulers.io)
      .consumeOnNext { (format, note) ->
        val formattedText = format.generateFrom(note)
        val exhaustive = when (format) {
          Html, RichText -> clipboard.copyRichText(formattedText)
          Markdown -> clipboard.copyPlainText(formattedText)
        }
      }
  }

  private fun handleShareClicks(
    events: Observable<EditorEvent>
  ): Observable<EditorUiModel> {
    val shareClicks = events.ofType<ShareAsClicked>().map { it.format }
    val noteChanges = events.ofType<NoteTextChanged>().map { it.text }

    return shareClicks.withLatestFrom(noteChanges)
      .observeOn(schedulers.io)
      .consumeOnNext { (format, note) ->
        val formattedText = format.generateFrom(note)
        val exhaustive = when (format) {
          Html, RichText -> intentLauncher.shareRichText(formattedText)
          Markdown -> intentLauncher.sharePlainText(formattedText)
        }
      }
  }

  private fun handleSplitScreenClicks(
    events: Observable<EditorEvent>,
    noteStream: Observable<Note>
  ): Observable<EditorUiModel> {
    return events.ofType<SplitScreenClicked>()
      .withLatestFrom(noteStream)
      .consumeOnNext { (_, note) ->
        args.navigator.splitScreenAndLfg(
          EditorScreenKey(ExistingNote(PreSavedNoteId(note.id)))
        )
      }
  }

  private fun handleDuplicateNoteClicks(
    events: Observable<EditorEvent>,
    noteStream: Observable<Note>
  ): Observable<EditorUiModel> {
    // It is important to use the text on the UI instead of the
    // one in the DB because it may have not been saved yet.
    val noteContent = events.ofType<NoteTextChanged>().map { it.text }

    return events.ofType<DuplicateNoteClicked>()
      .withLatestFrom(noteStream, noteContent)
      .observeOn(schedulers.io)
      .consumeOnNext { (_, note, content) ->
        val newNoteId = NoteId.generate()
        noteQueries.insert(
          id = newNoteId,
          folderId = note.folderId,
          content = content,
          createdAt = clock.nowUtc(),
          updatedAt = clock.nowUtc()
        )
        args.navigator.goBack()
        args.navigator.lfg(
          EditorScreenKey(NewNote(PreSavedNoteId(newNoteId)))
        )
      }
  }

  private fun TextFormat.generateFrom(noteContent: String): String {
    return when (this) {
      Markdown -> noteContent
      Html, RichText -> markdownParser.renderHtml(noteContent)
    }
  }

  private fun Observable<EditorEvent>.autoSaveContent(noteStream: Observable<Note>): Observable<EditorUiModel> {
    val textChanges = ofType<NoteTextChanged>().map { it.text }

    return noteStream
      .take(1)
      .flatMapCompletable { note ->
        observableInterval(config.autoSaveEvery, schedulers.computation)
          .withLatestFrom(textChanges) { _, text -> text }
          .distinctUntilChanged()
          .takeUntil(noteConflicts(noteStream))
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

    return noteQueries.note(noteId)
      .asObservable(schedulers.io)
      .mapToOneOrNull()
      .filterNotNull()
      .combineLatestWith(syncConflicts.isConflicted(noteId))
      .filter { (_, isConflicted) -> !isConflicted }
      .take(1)
      .flatMapCompletable { (note) ->
        completableFromFunction {
          if (shouldDelete) {
            noteQueries.markAsPendingDeletion(note.id)
          }
          noteQueries.updateContent(
            id = note.id,
            content = content,
            updatedAt = clock.nowUtc()
          )
        }
      }
  }

  fun interface Factory {
    fun create(args: Args): EditorPresenter
  }

  data class Args(
    val openMode: EditorOpenMode,
    /** Should be kept in sync with [HomePresenter.Args.includeBlankNotes]. */
    val deleteBlankNewNoteOnExit: Boolean,
    val navigator: Navigator,
    val onEffect: (EditorUiEffect) -> Unit
  )

  companion object {
    const val NEW_NOTE_PLACEHOLDER = "# "
  }
}

private fun String?.ifBlankOrNull(default: () -> String): String {
  return this?.ifBlank(default) ?: default()
}
