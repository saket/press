package me.saket.press.shared.editor

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
import me.saket.press.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER
import me.saket.press.shared.editor.EditorUiEffect.PopulateContent
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.note.FakeNoteRepository
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldNotBe
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
        openMode = openMode,
        noteRepository = repository,
        ioScheduler = trampolineScheduler,
        computationScheduler = testScheduler,
        strings = strings,
        config = config
    )
  }

  @Test fun `blank note is created on start when a new note is opened`() {
    repository.savedNotes shouldHaveSize 0

    val observer = presenter(NewNote(noteUuid))
        .uiModels(events)
        .test()

    val createdNote = repository.savedNotes.single()
    createdNote.uuid shouldEqual noteUuid
    createdNote.content shouldEqual ""

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
    savedNote().content shouldBe "# Ghost Rider"

    events.onNext(NoteTextChanged("# Ghost"))
    testScheduler.timer.advanceBy(config.autoSaveEvery.millisecondsLong)
    savedNote().content shouldBe "# Ghost"

    events.onNext(NoteTextChanged("# Ghost"))
    testScheduler.timer.advanceBy(config.autoSaveEvery.millisecondsLong)
    repository.updateCount shouldBe 2

    observer.assertNotError()
  }

  @Test fun `blank note is not created on start when an existing note is opened`() {
    repository.savedNotes += fakeNote(uuid = noteUuid, content = "Nicolas")

    val observer = presenter(ExistingNote(noteUuid))
        .uiModels(events)
        .test()

    val updatedNote = repository.savedNotes.single()
    updatedNote.uuid shouldEqual noteUuid
    updatedNote.content shouldEqual "Nicolas"

    observer.assertNotError()
  }

  @Test fun `blank note shouldn't be saved`() {
    val presenter = presenter(NewNote(noteUuid))

    presenter.saveEditorContentOnExit("  \n ")
    presenter.saveEditorContentOnExit("  ")
    presenter.saveEditorContentOnExit("")
    presenter.saveEditorContentOnExit(NEW_NOTE_PLACEHOLDER)
    presenter.saveEditorContentOnExit("  $NEW_NOTE_PLACEHOLDER ")

    assertTrue(repository.savedNotes.isEmpty())
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
    deletedNote.deletedAtString?.trim() shouldNotBe null
  }

  @Test fun `show new note placeholder on start`() {
    presenter(NewNote(noteUuid))
        .uiEffects()
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
        .uiEffects()
        .test()
        .apply {
          assertValue(PopulateContent("Nicolas Cage favorite dialogues"))
          assertNotError()
        }
  }
}
