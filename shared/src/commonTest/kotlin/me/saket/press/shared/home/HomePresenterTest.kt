package me.saket.press.shared.home

import assertk.assertThat
import assertk.assertions.containsOnly
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.test
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.home.HomePresenter.Args
import me.saket.press.shared.home.HomeUiEffect.ComposeNewNote
import me.saket.press.shared.home.HomeUiModel.Note
import me.saket.press.shared.note.FakeNoteRepository
import kotlin.test.Test

class HomePresenterTest {

  private val noteRepository = FakeNoteRepository()

  private fun presenter(includeEmptyNotes: Boolean = true) = HomePresenter(
      args = Args(includeEmptyNotes),
      repository = noteRepository
  )

  @Test fun `populate notes on creation`() {
    val noteId = NoteId.generate()
    noteRepository.savedNotes += listOf(
        fakeNote(
            noteId = noteId,
            localId = -1L,
            content = "# Nicolas Cage\nOur national treasure"
        )
    )

    val noteModel = presenter()
        .uiModels()
        .test()
        .values[0]
        .notes

    assertThat(noteModel).containsOnly(
        Note(
            noteId = noteId,
            adapterId = -1L,
            title = "Nicolas Cage",
            body = "Our national treasure"
        )
    )
  }

  @Test fun `filter out empty notes if requested`() {
    noteRepository.savedNotes += listOf(
        fakeNote(noteId = NoteId.generate(), content = "# Non-empty note"),
        fakeNote(noteId = NoteId.generate(), content = NEW_NOTE_PLACEHOLDER),
        fakeNote(noteId = NoteId.generate(), content = "")
    )

    presenter(includeEmptyNotes = false)
        .uiModels()
        .test()
        .apply {
          val titleAndBodies = values[0].notes.map { it.title to it.body }
          assertThat(titleAndBodies).containsOnly("Non-empty note" to "")
        }
  }

  @Test fun `include empty notes if requested`() {
    noteRepository.savedNotes += listOf(
        fakeNote(noteId = NoteId.generate(), content = "# Non-empty note"),
        fakeNote(noteId = NoteId.generate(), content = NEW_NOTE_PLACEHOLDER),
        fakeNote(noteId = NoteId.generate(), content = "")
    )

    presenter(includeEmptyNotes = true)
        .uiModels()
        .test()
        .apply {
          val titleAndBodies = values[0].notes.map { it.title to it.body }
          assertThat(titleAndBodies).containsOnly(
              "Non-empty note" to "",
              "" to "",
              "" to ""
          )
        }
  }

  @Test fun `open new note screen when new note is clicked`() {
    val presenter = presenter()

    presenter.uiEffects()
        .test()
        .also { presenter.dispatch(NewNoteClicked) }
        .assertValue(ComposeNewNote)
  }
}
