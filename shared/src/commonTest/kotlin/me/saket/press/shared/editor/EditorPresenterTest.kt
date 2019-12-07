package me.saket.press.shared.editor

import ch.tutteli.atrium.api.fluent.en_GB.hasSize
import ch.tutteli.atrium.api.fluent.en_GB.isEmpty
import ch.tutteli.atrium.api.fluent.en_GB.notToBeNull
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.badoo.reaktive.scheduler.trampolineScheduler
import com.badoo.reaktive.subject.publish.publishSubject
import com.badoo.reaktive.test.base.assertNotError
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.test
import com.badoo.reaktive.test.scheduler.TestScheduler
import com.benasher44.uuid.uuid4
import com.soywiz.klock.seconds
import me.saket.press.shared.editor.EditorEvent.NoteTextChanged
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorPresenter.Args
import me.saket.press.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER
import me.saket.press.shared.editor.EditorUiEffect.PopulateContent
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.note.FakeNoteRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditorPresenterTest {

  private val noteUuid = uuid4()
  private val repository = FakeNoteRepository()
  private val testScheduler = TestScheduler()
  private val config = EditorConfig(autoSaveEvery = 5.seconds)
  private val strings = Strings.Editor(
      newNoteHints = listOf("New note heading hint #1", "New note heading hint #2")
  )

  private val events = publishSubject<EditorEvent>()

  private fun presenter(openMode: EditorOpenMode): EditorPresenter {
    return EditorPresenter(
        args = Args(openMode),
        noteRepository = repository,
        ioScheduler = trampolineScheduler,
        computationScheduler = testScheduler,
        strings = strings,
        config = config
    )
  }

  @Test fun `blank note is created on start when a new note is opened`() {
    expect(repository.savedNotes).hasSize(0)

    val observer = presenter(NewNote(noteUuid))
        .uiModels(events)
        .test()

    repository.savedNotes.single().let {
      expect(it.uuid).toBe(noteUuid)
      expect(it.content).isEmpty()
    }
    observer.assertNotError()
  }

  @Test fun `auto-save note at regular intervals`() {
    repository.savedNotes += fakeNote(
        uuid = noteUuid,
        content = "# "
    )

    val observer = presenter(NewNote(noteUuid))
        .uiModels(events)
        .test()

    val savedNote = { repository.savedNotes.single { it.uuid == noteUuid } }

    events.onNext(NoteTextChanged("# Ghost Rider"))
    testScheduler.timer.advanceBy(config.autoSaveEvery.millisecondsLong)
    expect(savedNote().content).toBe("# Ghost Rider")

    events.onNext(NoteTextChanged("# Ghost"))
    testScheduler.timer.advanceBy(config.autoSaveEvery.millisecondsLong)
    expect(savedNote().content).toBe("# Ghost")

    events.onNext(NoteTextChanged("# Ghost"))
    testScheduler.timer.advanceBy(config.autoSaveEvery.millisecondsLong)
    expect(repository.updateCount).toBe(2)

    observer.assertNotError()
  }

  @Test fun `blank note is not created on start when an existing note is opened`() {
    repository.savedNotes += fakeNote(uuid = noteUuid, content = "Nicolas")

    val observer = presenter(ExistingNote(noteUuid))
        .uiModels(events)
        .test()

    repository.savedNotes.single().let {
      expect(it.uuid).toBe(noteUuid)
      expect(it.content).toBe("Nicolas")
    }
    observer.assertNotError()
  }

  @Test fun `updating an existing note on exit when its content is non-blank`() {
    repository.savedNotes += fakeNote(
        uuid = noteUuid,
        content = "Existing note"
    )

    val presenter = presenter(NewNote(noteUuid))
    presenter.saveEditorContentOnExit("Updated note")

    val savedNote = repository.savedNotes.last()
    assertEquals("Updated note", savedNote.content)
  }

  @Test fun `deleting an existing note on exit when its content is blank`() {
    repository.savedNotes += fakeNote(
        uuid = noteUuid,
        content = "Existing note"
    )

    val presenter = presenter(NewNote(noteUuid))
    presenter.saveEditorContentOnExit("  \n ")
    presenter.saveEditorContentOnExit("  ")
    presenter.saveEditorContentOnExit("")

    val deletedNote = repository.savedNotes.last()
    expect(deletedNote.deletedAtString).notToBeNull()
  }

  @Test fun `show new note placeholder on start`() {
    presenter(NewNote(noteUuid))
        .uiEffects(events)
        .test()
        .apply {
          assertEquals(values[0], PopulateContent(NEW_NOTE_PLACEHOLDER))
        }
        .assertNotError()
  }

  @Test fun `show hint text until the text is changed`() {
    val uiModels = presenter(NewNote(noteUuid))
        .uiModels(events)
        .test()

    events.onNext(NoteTextChanged(NEW_NOTE_PLACEHOLDER))
    events.onNext(NoteTextChanged(""))
    events.onNext(NoteTextChanged(NEW_NOTE_PLACEHOLDER))
    events.onNext(NoteTextChanged("  $NEW_NOTE_PLACEHOLDER"))
    events.onNext(NoteTextChanged("$NEW_NOTE_PLACEHOLDER  "))

    val randomlySelectedHint = uiModels.values[0].hintText

    assertTrue(randomlySelectedHint in strings.newNoteHints)
    assertEquals(null, uiModels.values[1].hintText)
    assertEquals(randomlySelectedHint, uiModels.values[2].hintText)
    assertEquals(null, uiModels.values[3].hintText)
    assertEquals(randomlySelectedHint, uiModels.values[4].hintText)

    uiModels.assertNotError()
  }

  @Test fun `populate note content on start`() {
    repository.savedNotes += fakeNote(
        uuid = noteUuid,
        content = "Nicolas Cage favorite dialogues"
    )

    presenter(ExistingNote(noteUuid))
        .uiEffects(events)
        .test()
        .apply {
          assertValue(PopulateContent("Nicolas Cage favorite dialogues"))
          assertNotError()
        }
  }
}
