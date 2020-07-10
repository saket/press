package me.saket.press.shared.editor

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isIn
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.badoo.reaktive.test.base.assertNotError
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.test
import com.badoo.reaktive.test.scheduler.TestScheduler
import com.soywiz.klock.seconds
import me.saket.press.shared.FakeSchedulers
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorPresenter.Args
import me.saket.press.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER
import me.saket.press.shared.editor.EditorUiEffect.UpdateNoteText
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.localization.ENGLISH_STRINGS
import me.saket.press.shared.note.FakeNoteRepository
import me.saket.wysiwyg.formatting.TextSelection
import kotlin.test.Test
import kotlin.test.assertEquals

class EditorPresenterTest {
  private val noteId = NoteId.generate()
  private val repository = FakeNoteRepository()
  private val testScheduler = TestScheduler()
  private val config = EditorConfig(autoSaveEvery = 5.seconds)

  private fun presenter(
    openMode: EditorOpenMode,
    archiveEmptyNoteOnExit: Boolean = true
  ): EditorPresenter {
    return EditorPresenter(
        args = Args(openMode, archiveEmptyNoteOnExit),
        noteRepository = repository,
        schedulers = FakeSchedulers(computation = testScheduler),
        strings = ENGLISH_STRINGS,
        config = config
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
    testScheduler.timer.advanceBy(config.autoSaveEvery.millisecondsLong)
    assertThat(savedNote().content).isEqualTo("# Ghost Rider")

    presenter.dispatch(NoteTextChanged("# Ghost"))
    testScheduler.timer.advanceBy(config.autoSaveEvery.millisecondsLong)
    assertThat(savedNote().content).isEqualTo("# Ghost")

    presenter.dispatch(NoteTextChanged("# Ghost"))
    testScheduler.timer.advanceBy(config.autoSaveEvery.millisecondsLong)
    assertThat(repository.updateCount).isEqualTo(2)

    observer.assertNotError()
  }

  @Test fun `blank note is not created on start when an existing note is opened`() {
    repository.savedNotes += fakeNote(id = noteId, content = "Nicolas")

    val observer = presenter(ExistingNote(noteId))
        .uiModels()
        .test()

    repository.savedNotes.single().let {
      assertThat(it.id).isEqualTo(noteId)
      assertThat(it.content).isEqualTo("Nicolas")
    }
    observer.assertNotError()
  }

  @Test fun `updating an existing note on exit when its content is non-blank`() {
    repository.savedNotes += fakeNote(
        id = noteId,
        content = "Existing note"
    )

    val presenter = presenter(NewNote(noteId))
    presenter.saveEditorContentOnExit("Updated note")

    val savedNote = repository.savedNotes.last()
    assertEquals("Updated note", savedNote.content)
  }

  @Test fun `archive blank note on exit when enabled`() {
    repository.savedNotes += fakeNote(
        id = noteId,
        content = "Existing note"
    )

    val presenter = presenter(ExistingNote(noteId), archiveEmptyNoteOnExit = true)
    presenter.saveEditorContentOnExit("  \n ")
    presenter.saveEditorContentOnExit("  ")
    presenter.saveEditorContentOnExit("")

    val archivedNote = repository.savedNotes.last()
    assertThat(archivedNote.content).isEqualTo("")
    assertThat(archivedNote.isArchived).isTrue()
  }

  @Test fun `avoid archiving blank note on exit when disabled`() {
    repository.savedNotes += fakeNote(
        id = noteId,
        content = "Existing note"
    )

    val presenter = presenter(ExistingNote(noteId), archiveEmptyNoteOnExit = false)
    presenter.saveEditorContentOnExit("")

    val archivedNote = repository.savedNotes.last()
    assertThat(archivedNote.content).isEqualTo("")
    assertThat(archivedNote.isArchived).isFalse()
  }

  @Test fun `show hint text until the text is changed`() {
    val presenter = presenter(NewNote(noteId))
    val uiModels = presenter
        .uiModels()
        .test()

    val hintText = { uiModels.values.last().hintText?.drop("# ".length) }

    presenter.dispatch(NoteTextChanged(NEW_NOTE_PLACEHOLDER))
    assertThat(hintText()).isIn(ENGLISH_STRINGS.editor.new_note_hints)

    presenter.dispatch(NoteTextChanged(""))
    assertThat(hintText()).isNull()

    presenter.dispatch(NoteTextChanged(NEW_NOTE_PLACEHOLDER))
    assertThat(hintText()).isIn(ENGLISH_STRINGS.editor.new_note_hints)

    presenter.dispatch(NoteTextChanged("  $NEW_NOTE_PLACEHOLDER"))
    assertThat(hintText()).isNull()

    presenter.dispatch(NoteTextChanged("$NEW_NOTE_PLACEHOLDER  "))
    assertThat(hintText()).isIn(ENGLISH_STRINGS.editor.new_note_hints)

    uiModels.assertNotError()
  }

  @Test fun `populate existing note's content on start`() {
    repository.savedNotes += fakeNote(
        id = noteId,
        content = "Nicolas Cage favorite dialogues"
    )

    presenter(ExistingNote(noteId))
        .uiEffects()
        .test()
        .apply {
          assertValue(UpdateNoteText("Nicolas Cage favorite dialogues", newSelection = null))
          assertNotError()
        }
  }

  @Test fun `populate new note's content with placeholder on start`() {
    presenter(NewNote(noteId))
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
    presenter(NewNote(noteId, note))
        .uiEffects()
        .test()
        .apply {
          assertValue(UpdateNoteText(newText = note, newSelection = null))
          assertNotError()
        }
  }
}
