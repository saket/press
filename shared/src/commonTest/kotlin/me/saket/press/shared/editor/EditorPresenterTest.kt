package me.saket.press.shared.editor

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
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
import me.saket.press.shared.db.BaseDatabaeTest
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
import me.saket.press.shared.rx.RxRule
import me.saket.press.shared.rx.test
import me.saket.press.shared.sync.SyncMergeConflicts
import me.saket.press.shared.sync.SyncState.IN_FLIGHT
import me.saket.press.shared.sync.git.DelegatingPressDatabase
import me.saket.press.shared.time.FakeClock
import me.saket.press.shared.ui.FakeNavigator
import me.saket.wysiwyg.formatting.TextSelection
import kotlin.test.AfterTest
import kotlin.test.Test

class EditorPresenterTest : BaseDatabaeTest() {
  override val database = DelegatingPressDatabase(super.database)
  private val noteQueries get() = database.noteQueries
  private val rxRule = RxRule()

  private val noteId = NoteId.generate()
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
      database = database,
      clock = FakeClock(),
      schedulers = FakeSchedulers(computation = testScheduler),
      strings = ENGLISH_STRINGS,
      config = config,
      syncConflicts = syncConflicts
    )
  }

  @AfterTest
  fun finish() {
    rxRule.assertEmpty()
  }

  @Test fun `blank note is created on start when a new note is opened`() {
    assertThat(noteQueries.allNotes().executeAsList()).hasSize(0)

    presenter(NewNote(noteId))
      .uiModels()
      .test(rxRule)

    noteQueries.allNotes().executeAsOne().let {
      assertThat(it.id).isEqualTo(noteId)
      assertThat(it.content).isEqualTo(NEW_NOTE_PLACEHOLDER)
    }
  }

  @Test fun `auto-save note at regular intervals`() {
    noteQueries.testInsert(fakeNote("# ", id = noteId))

    val presenter = presenter(NewNote(noteId))
    val models = presenter.uiModels().test(rxRule)

    val savedNote = { noteQueries.allNotes().executeAsOne() }

    presenter.dispatch(NoteTextChanged("# Ghost Rider"))
    testScheduler.timer.advanceBy(config.autoSaveEvery)
    assertThat(savedNote().content).isEqualTo("# Ghost Rider")

    presenter.dispatch(NoteTextChanged("# Ghost"))
    testScheduler.timer.advanceBy(config.autoSaveEvery)
    assertThat(savedNote().content).isEqualTo("# Ghost")

    presenter.dispatch(NoteTextChanged("# Ghost"))
    testScheduler.timer.advanceBy(config.autoSaveEvery)
    assertThat(noteQueries.updateCount).isEqualTo(2)

    repeat(2) {
      models.assertAnyValue()
    }
  }

  @Test fun `blank note is not created on start when an existing note is opened`() {
    noteQueries.testInsert(fakeNote("Nicolas", id = noteId))

    presenter(ExistingNote(PreSavedNoteId(noteId)))
      .uiModels()
      .test(rxRule)

    noteQueries.allNotes().executeAsOne().let {
      assertThat(it.id).isEqualTo(noteId)
      assertThat(it.content).isEqualTo("Nicolas")
    }
  }

  @Test fun `updating an existing note on close when its content is non-blank`() {
    noteQueries.testInsert(fakeNote("Existing note", id = noteId))

    presenter(NewNote(noteId))
      .saveEditorContentOnClose("Updated note")
      .test(rxRule)
      .assertComplete()

    val savedNote = noteQueries.allNotes().executeAsOne()
    assertThat(savedNote.content).isEqualTo("Updated note")
  }

  @Test fun `delete new blank notes on close when enabled`() {
    val presenter = presenter(
      openMode = NewNote(PlaceholderNoteId(NoteId.generate()), preFilledNote = "# Nicolas Cage"),
      deleteBlankNoteOnExit = true
    )
    presenter.uiModels().test()

    presenter.dispatch(NoteTextChanged("# Nicolas Cage"))
    testScheduler.timer.advanceBy(config.autoSaveEvery)

    val savedNote = { noteQueries.allNotes().executeAsOne() }
    assertThat(savedNote().content).isEqualTo("# Nicolas Cage")

    presenter.saveEditorContentOnClose("").test(rxRule).assertComplete()
    presenter.saveEditorContentOnClose("  ").test(rxRule).assertComplete()
    presenter.saveEditorContentOnClose("  \n ").test(rxRule).assertComplete()
    assertThat(savedNote().content).isEqualTo("  \n ")
    assertThat(savedNote().isPendingDeletion).isTrue()
  }

  @Test fun `deletion of note shouldn't cause any error`() {
    val note = fakeNote("# The")
    noteQueries.testInsert(note)

    val presenter = presenter(ExistingNote(PreSavedNoteId(note.id)))
    val models = presenter.uiModels().test(rxRule)

    noteQueries.run {
      markAsPendingDeletion(note.id)
      updateSyncState(ids = listOf(note.id), syncState = IN_FLIGHT)
      deleteNote(note.id)
    }

    presenter.dispatch(NoteTextChanged("# The Witcher"))
    testScheduler.timer.advanceBy(config.autoSaveEvery)
    models.assertAnyValue()

    presenter.saveEditorContentOnClose("# The Witcher 3").test(rxRule)

    assertThat(noteQueries.allNotes().executeAsList()).isEmpty()
  }

  @Test fun `avoid deleting new blank note on close when disabled`() {
    val presenter = presenter(
      openMode = NewNote(PreSavedNoteId(noteId), preFilledNote = "# Nicolas Cage"),
      deleteBlankNoteOnExit = false
    )
    presenter.uiModels().test()

    presenter.dispatch(NoteTextChanged("# Nicolas Cage"))
    testScheduler.timer.advanceBy(config.autoSaveEvery)
    val savedNote = { noteQueries.allNotes().executeAsOne() }
    assertThat(savedNote().content).isEqualTo("# Nicolas Cage")

    presenter.saveEditorContentOnClose("").test(rxRule).assertComplete()
    assertThat(savedNote().content).isEqualTo("")
    assertThat(savedNote().isPendingDeletion).isFalse()
  }

  @Test fun `avoid deleting existing blank note on close when enabled`() {
    noteQueries.testInsert(fakeNote("Existing note", id = noteId))

    val presenter = presenter(ExistingNote(PreSavedNoteId(noteId)), deleteBlankNoteOnExit = true)
    presenter.saveEditorContentOnClose("").test(rxRule).assertComplete()
    presenter.saveEditorContentOnClose("  ").test(rxRule).assertComplete()
    presenter.saveEditorContentOnClose("  \n ").test(rxRule).assertComplete()

    noteQueries.allNotes().executeAsOne().let {
      assertThat(it.content).isEqualTo("  \n ")
      assertThat(it.isPendingDeletion).isFalse()
    }
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
    noteQueries.testInsert(fakeNote("Nicolas Cage favorite dialogues", id = noteId))

    presenter(ExistingNote(PreSavedNoteId(noteId)))
      .uiEffects()
      .test(rxRule)
      .assertValue(
        UpdateNoteText("Nicolas Cage favorite dialogues", newSelection = null)
      )
  }

  @Test fun `populate new note's content with placeholder on start`() {
    presenter(NewNote(PlaceholderNoteId(noteId), preFilledNote = "   "))
      .uiEffects()
      .test()
      .assertValue(
        UpdateNoteText(
          newText = NEW_NOTE_PLACEHOLDER,
          newSelection = TextSelection.cursor(NEW_NOTE_PLACEHOLDER.length)
        )
      )
  }

  @Test fun `populate new note's content with pre-filled note on start`() {
    val note = "Hello, World!"
    presenter(NewNote(PlaceholderNoteId(noteId), note))
      .uiEffects()
      .test(rxRule)
      .assertValue(UpdateNoteText(newText = note, newSelection = null))
  }

  @Test fun `block editing if note is marked as sync-conflicted`() {
    noteQueries.testInsert(fakeNote("# Existing note", id = noteId))

    presenter(ExistingNote(PreSavedNoteId(noteId))).uiEffects()
      .ofType<BlockedDueToSyncConflict>()
      .test(rxRule)
      .also { syncConflicts.add(noteId) }
      .assertValue(BlockedDueToSyncConflict)
  }

  @Test fun `stop updating saved note once note is marked as sync-conflicted`() {
    noteQueries.testInsert(fakeNote("# Content before sync", id = noteId))

    val presenter = presenter(ExistingNote(PreSavedNoteId(noteId)))
    presenter.uiModels().test()
    syncConflicts.add(noteId)

    presenter.dispatch(NoteTextChanged("# Updated note"))
    presenter.saveEditorContentOnClose("# Exitingggggg").test(rxRule)

    val savedNote = noteQueries.allNotes().executeAsOne()
    assertThat(savedNote.content).isEqualTo("# Content before sync")
  }
}

@Suppress("TestFunctionName")
private fun NewNote(id: NoteId) = NewNote(PlaceholderNoteId(id), preFilledNote = null)

private fun TestScheduler.Timer.advanceBy(timeSpan: TimeSpan) {
  advanceBy(timeSpan.millisecondsLong)
}
