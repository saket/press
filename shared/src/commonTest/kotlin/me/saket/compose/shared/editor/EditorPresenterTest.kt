package me.saket.compose.shared.editor

import com.badoo.reaktive.scheduler.trampolineScheduler
import com.badoo.reaktive.subject.publish.publishSubject
import com.benasher44.uuid.uuid4
import me.saket.compose.shared.ENGLISH_STRINGS
import me.saket.compose.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER
import me.saket.compose.shared.fakedata.fakeNote
import me.saket.compose.shared.note.FakeNoteRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditorPresenterTest {

  private val noteUuid = uuid4()
  private val repository = FakeNoteRepository()

  private val events = publishSubject<EditorEvent>()
  private val presenter = EditorPresenter(
      noteUuid = noteUuid,
      noteRepository = repository,
      ioScheduler = trampolineScheduler,
      strings = ENGLISH_STRINGS.editor
  )

  @Test fun `blank note shouldn't be saved`() {
    presenter.saveEditorContentOnExit("  \n ")
    presenter.saveEditorContentOnExit("  ")
    presenter.saveEditorContentOnExit("")
    presenter.saveEditorContentOnExit(NEW_NOTE_PLACEHOLDER)
    presenter.saveEditorContentOnExit("  $NEW_NOTE_PLACEHOLDER ")

    assertTrue(repository.savedNotes.isEmpty())
  }

  @Test fun `creating a new note`() {
    presenter.saveEditorContentOnExit("New note")

    val savedNote = repository.savedNotes.last()
    assertEquals("New note", savedNote.content)
  }

  @Test fun `updating an existing note`() {
    repository.savedNotes += fakeNote(
        uuid = noteUuid,
        content = "Existing note"
    )

    presenter.saveEditorContentOnExit("Updated note")

    val savedNote = repository.savedNotes.last()
    assertEquals("Updated note", savedNote.content)
  }

  @Test fun `deleting a note when content is blank`() {
    repository.savedNotes += fakeNote(
        uuid = noteUuid,
        content = "Existing note"
    )

    presenter.saveEditorContentOnExit("  \n ")
    presenter.saveEditorContentOnExit("  ")
    presenter.saveEditorContentOnExit("")

    assertTrue(repository.savedNotes.isEmpty())
  }

//  @Test fun `show hint text until the text is changed`() {
//    val contentModels = presenter.contentModels(events).test()
//
//    events.onNext(NoteTextChanged(NEW_NOTE_PLACEHOLDER))
//    events.onNext(NoteTextChanged(""))
//    events.onNext(NoteTextChanged(NEW_NOTE_PLACEHOLDER))
//
//    assertEquals(ENGLISH_STRINGS.editor.newNoteHint, contentModels.values[0].hintText)
//    assertEquals(null, contentModels.values[1].hintText)
//    assertEquals(null, contentModels.values[2].hintText)
//  }
}
