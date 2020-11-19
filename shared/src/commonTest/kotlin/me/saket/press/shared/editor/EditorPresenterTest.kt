package me.saket.press.shared.editor

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.test.base.assertNotError
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.test
import com.badoo.reaktive.test.scheduler.TestScheduler
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import me.saket.press.shared.FakeSchedulers
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorPresenter.Args
import me.saket.press.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER
import me.saket.press.shared.editor.EditorUiEffect.BlockedDueToSyncConflict
import me.saket.press.shared.editor.EditorUiEffect.UpdateNoteText
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.localization.ENGLISH_STRINGS
import me.saket.press.shared.note.FakeNoteRepository
import me.saket.press.shared.sync.SyncMergeConflicts
import me.saket.press.shared.ui.FakeNavigator
import me.saket.wysiwyg.formatting.TextSelection
import kotlin.test.Test
import kotlin.test.assertEquals

class EditorPresenterTest {
  private val noteId = NoteId.generate()
  private val repository = FakeNoteRepository()
  private val testScheduler = TestScheduler()
  private val config = EditorConfig(autoSaveEvery = 5.seconds)
  private val navigator = FakeNavigator()
  private val syncConflicts = SyncMergeConflicts()

  private fun presenter(
    openMode: EditorOpenMode,
    deleteBlankNoteOnExit: Boolean = true
  ): EditorPresenter {
    return EditorPresenter(
      args = Args(openMode, deleteBlankNoteOnExit, navigator),
      noteRepository = repository,
      schedulers = FakeSchedulers(computation = testScheduler),
      strings = ENGLISH_STRINGS,
      config = config,
      syncConflicts = syncConflicts
    )
  }

  @Test fun `blank note is created on start when a new note is opened`() {
    assertThat(repository.savedNotes).hasSize(0)

    val observer = presenter(NewNote(noteId))
      .uiModels()
      .test()

    repository.savedNotes.single().let {
      assertThat(it.id).isEqualTo(noteId)
      assertThat(it.content).isEqualTo(NEW_NOTE_PLACEHOLDER)
    }
    observer.assertNotError()
  }

  @Test fun `auto-save note at regular intervals`() {
    repository.savedNotes += fakeNote(
      id = noteId,
      content = "# "
    )

    val presenter = presenter(NewNote(noteId))
    val observer = presenter
      .uiModels()
      .test()

    val savedNote = { repository.savedNotes.single { it.id == noteId } }

    presenter.dispatch(NoteTextChanged("# Ghost Rider"))
    testScheduler.timer.advanceBy(config.autoSaveEvery)
    assertThat(savedNote().content).isEqualTo("# Ghost Rider")

    presenter.dispatch(NoteTextChanged("# Ghost"))
    testScheduler.timer.advanceBy(config.autoSaveEvery)
    assertThat(savedNote().content).isEqualTo("# Ghost")

    presenter.dispatch(NoteTextChanged("# Ghost"))
    testScheduler.timer.advanceBy(config.autoSaveEvery)
    assertThat(repository.updateCount).isEqualTo(2)

    observer.assertNotError()
  }

  @Test fun `blank note is not created on start when an existing note is opened`() {
    repository.savedNotes += fakeNote(id = noteId, content = "Nicolas")

    val observer = presenter(ExistingNote(PreSavedNoteId(noteId), -1))
      .uiModels()
      .test()

    repository.savedNotes.single().let {
      assertThat(it.id).isEqualTo(noteId)
      assertThat(it.content).isEqualTo("Nicolas")
    }
    observer.assertNotError()
  }

  @Test fun `updating an existing note on close when its content is non-blank`() {
    repository.savedNotes += fakeNote(
      id = noteId,
      content = "Existing note"
    )

    val presenter = presenter(NewNote(noteId))
    presenter.saveEditorContentOnClose("Updated note")

    val savedNote = repository.savedNotes.last()
    assertEquals("Updated note", savedNote.content)
  }

  @Test fun `delete new blank notes on close when enabled`() {
    val presenter = presenter(
      openMode = NewNote(PlaceholderNoteId(NoteId.generate()), preFilledNote = "# Nicolas Cage"),
      deleteBlankNoteOnExit = true
    )
    presenter.uiModels().test()

    presenter.dispatch(NoteTextChanged("# Nicolas Cage"))
    testScheduler.timer.advanceBy(config.autoSaveEvery)

    val savedNote = { repository.savedNotes.single() }
    assertThat(savedNote().content).isEqualTo("# Nicolas Cage")

    presenter.saveEditorContentOnClose("")
    presenter.saveEditorContentOnClose("  ")
    presenter.saveEditorContentOnClose("  \n ")
    assertThat(savedNote().content).isEqualTo("  \n ")
    assertThat(savedNote().isPendingDeletion).isTrue()
  }

  @Test fun `avoid deleting new blank note on close when disabled`() {
    val presenter = presenter(
      openMode = NewNote(PreSavedNoteId(noteId), preFilledNote = "# Nicolas Cage"),
      deleteBlankNoteOnExit = false
    )
    presenter.uiModels().test()

    presenter.dispatch(NoteTextChanged("# Nicolas Cage"))
    testScheduler.timer.advanceBy(config.autoSaveEvery)
    val savedNote = { repository.savedNotes.single() }
    assertThat(savedNote().content).isEqualTo("# Nicolas Cage")

    presenter.saveEditorContentOnClose("")
    assertThat(savedNote().content).isEqualTo("")
    assertThat(savedNote().isPendingDeletion).isFalse()
  }

  @Test fun `avoid deleting existing blank note on close when enabled`() {
    repository.savedNotes += fakeNote(id = noteId, content = "Existing note")

    val presenter = presenter(ExistingNote(PreSavedNoteId(noteId), -1), deleteBlankNoteOnExit = true)
    presenter.saveEditorContentOnClose("")
    presenter.saveEditorContentOnClose("  ")
    presenter.saveEditorContentOnClose("  \n ")

    val savedNote = repository.savedNotes.single()
    assertThat(savedNote.content).isEqualTo("  \n ")
    assertThat(savedNote.isPendingDeletion).isFalse()
  }

  @Test fun `show hint text until the text is changed`() {
    val presenter = presenter(NewNote(noteId))
    val uiModels = presenter
      .uiModels()
      .test()

    val hintText = { uiModels.values.last().hintText?.drop("# ".length) }

    presenter.dispatch(NoteTextChanged(NEW_NOTE_PLACEHOLDER))
    assertThat(ENGLISH_STRINGS.editor.new_note_hints).contains(hintText())

    presenter.dispatch(NoteTextChanged(""))
    assertThat(hintText()).isNull()

    presenter.dispatch(NoteTextChanged(NEW_NOTE_PLACEHOLDER))
    assertThat(ENGLISH_STRINGS.editor.new_note_hints).contains(hintText())

    presenter.dispatch(NoteTextChanged("  $NEW_NOTE_PLACEHOLDER"))
    assertThat(hintText()).isNull()

    presenter.dispatch(NoteTextChanged("$NEW_NOTE_PLACEHOLDER  "))
    assertThat(ENGLISH_STRINGS.editor.new_note_hints).contains(hintText())

    uiModels.assertNotError()
  }

  @Test fun `populate existing note's content on start`() {
    repository.savedNotes += fakeNote(
      id = noteId,
      content = "Nicolas Cage favorite dialogues"
    )

    presenter(ExistingNote(PreSavedNoteId(noteId), -1))
      .uiEffects()
      .test()
      .apply {
        assertValue(UpdateNoteText("Nicolas Cage favorite dialogues", newSelection = null))
        assertNotError()
      }
  }

  @Test fun `populate new note's content with placeholder on start`() {
    presenter(NewNote(PlaceholderNoteId(noteId), preFilledNote = "   "))
      .uiEffects()
      .test()
      .apply {
        assertValue(
          UpdateNoteText(
            newText = NEW_NOTE_PLACEHOLDER,
            newSelection = TextSelection.cursor(NEW_NOTE_PLACEHOLDER.length)
          )
        )
        assertNotError()
      }
  }

  @Test fun `populate new note's content with pre-filled note on start`() {
    val note = "Hello, World!"
    presenter(NewNote(PlaceholderNoteId(noteId), note))
      .uiEffects()
      .test()
      .apply {
        assertValue(UpdateNoteText(newText = note, newSelection = null))
        assertNotError()
      }
  }

  @Test fun `block editing if note is marked as sync-conflicted`() {
    repository.savedNotes += fakeNote(id = noteId, content = "# Existing note")
    presenter(ExistingNote(PreSavedNoteId(noteId), -1)).uiEffects()
      .ofType<BlockedDueToSyncConflict>()
      .test()
      .also { syncConflicts.add(noteId) }
      .assertValue(BlockedDueToSyncConflict)
  }

  @Test fun `stop updating saved note once note is marked as sync-conflicted`() {
    repository.savedNotes += fakeNote(id = noteId, content = "# Content before sync")

    val presenter = presenter(ExistingNote(PreSavedNoteId(noteId), -1))
    presenter.uiModels().test()
    syncConflicts.add(noteId)

    presenter.dispatch(NoteTextChanged("# Updated note"))
    presenter.saveEditorContentOnClose("# Exitingggggg")

    val savedNote = repository.savedNotes.single()
    assertThat(savedNote.content).isEqualTo("# Content before sync")
  }
}

@Suppress("TestFunctionName")
private fun NewNote(id: NoteId) = NewNote(PlaceholderNoteId(id), preFilledNote = null)

private fun TestScheduler.Timer.advanceBy(timeSpan: TimeSpan) {
  advanceBy(timeSpan.millisecondsLong)
}
