package me.saket.compose.shared.editor

import com.badoo.reaktive.scheduler.trampolineScheduler
import com.benasher44.uuid.uuid4
import me.saket.compose.shared.fakedata.fakeNote
import me.saket.compose.shared.note.FakeNoteRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditorPresenterTest {

  private val noteUuid = uuid4()
  private val repository = FakeNoteRepository()

  private val presenter = EditorPresenter(
      noteUuid = noteUuid,
      noteRepository = repository,
      ioScheduler = trampolineScheduler
  )

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

    assertTrue(repository.savedNotes.isEmpty())
  }
}
