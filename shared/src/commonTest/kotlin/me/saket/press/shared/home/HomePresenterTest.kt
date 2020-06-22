package me.saket.press.shared.home

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isNotEmpty
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.test.observable.assertValue
import com.badoo.reaktive.test.observable.test
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.home.HomePresenter.Args
import me.saket.press.shared.home.HomeUiEffect.ComposeNewNote
import me.saket.press.shared.home.HomeUiModel.Note
import me.saket.press.shared.keyboard.KeyboardShortcuts
import me.saket.press.shared.keyboard.RealKeyboardShortcuts
import me.saket.press.shared.note.FakeNoteRepository
import kotlin.test.Test

class HomePresenterTest {

  private val noteRepository = FakeNoteRepository()
  private val keyboardShortcuts = RealKeyboardShortcuts()

  private fun presenter(includeEmptyNotes: Boolean = true): HomePresenter {
    return HomePresenter(
        args = Args(includeEmptyNotes),
        repository = noteRepository,
        keyboardShortcuts = keyboardShortcuts
    )
  }

  @Test fun `populate notes on creation`() {
    val noteId = NoteId.generate()
    noteRepository.savedNotes += listOf(
        fakeNote(
            id = noteId,
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
        fakeNote(id = NoteId.generate(), content = "# Non-empty note"),
        fakeNote(id = NoteId.generate(), content = NEW_NOTE_PLACEHOLDER),
        fakeNote(id = NoteId.generate(), content = "")
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
        fakeNote(id = NoteId.generate(), content = "# Non-empty note"),
        fakeNote(id = NoteId.generate(), content = NEW_NOTE_PLACEHOLDER),
        fakeNote(id = NoteId.generate(), content = "")
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

    val effects = presenter.uiEffects()
        .ofType<ComposeNewNote>()
        .test()

    assertThat(noteRepository.savedNotes).isEmpty()

    presenter.dispatch(NewNoteClicked)

    assertThat(noteRepository.savedNotes).isNotEmpty()
    val savedNoteId = noteRepository.savedNotes.single().id
    effects.assertValue(ComposeNewNote(noteId = savedNoteId))
  }

  @Test fun `open new note screen on new-note keyboard shortcut`() {
    val effects = presenter().uiEffects()
        .ofType<ComposeNewNote>()
        .test()

    keyboardShortcuts.broadcast(KeyboardShortcuts.newNote)

    val savedNoteId = noteRepository.savedNotes.single().id
    effects.assertValue(ComposeNewNote(noteId = savedNoteId))
  }
}
