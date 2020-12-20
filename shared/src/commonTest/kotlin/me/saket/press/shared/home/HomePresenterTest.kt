package me.saket.press.shared.home

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.test.observable.test
import com.soywiz.klock.hours
import me.saket.press.shared.FakeSchedulers
import me.saket.press.shared.db.BaseDatabaeTest
import me.saket.press.shared.db.FolderId
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.editor.NoteIdKind
import me.saket.press.shared.editor.PreSavedNoteId
import me.saket.press.shared.fakedata.fakeFolder
import me.saket.press.shared.fakedata.fakeNote
import me.saket.press.shared.home.HomeEvent.NewNoteClicked
import me.saket.press.shared.home.HomePresenter.Args
import me.saket.press.shared.home.HomeUiModel.Note
import me.saket.press.shared.keyboard.KeyboardShortcuts
import me.saket.press.shared.keyboard.RealKeyboardShortcuts
import me.saket.press.shared.localization.ENGLISH_STRINGS
import me.saket.press.shared.rx.RxRule
import me.saket.press.shared.rx.test
import me.saket.press.shared.sync.git.insert
import me.saket.press.shared.sync.git.testInsert
import me.saket.press.shared.time.FakeClock
import me.saket.press.shared.ui.FakeNavigator
import kotlin.test.AfterTest
import kotlin.test.Test

class HomePresenterTest : BaseDatabaeTest() {
  private val keyboardShortcuts = RealKeyboardShortcuts()
  private val navigator = FakeNavigator()
  private val rxRule = RxRule()
  private val clock = FakeClock()
  private val noteQueries get() = database.noteQueries
  private val folderQueries get() = database.folderQueries

  private fun presenter(
    folder: FolderId? = null,
    includeEmptyNotes: Boolean = true
  ): HomePresenter {
    return HomePresenter(
      args = Args(HomeScreenKey(folder = folder), includeEmptyNotes, navigator),
      keyboardShortcuts = keyboardShortcuts,
      database = database,
      schedulers = FakeSchedulers(),
      strings = ENGLISH_STRINGS,
      clock = clock
    )
  }

  @AfterTest
  fun finish() {
    rxRule.assertEmpty()
  }

  @Test fun `populate folders and notes for home screen`() {
    val archive = fakeFolder("archive")
    folderQueries.insert(archive)

    val witcher3 = fakeNote(
      content = "The Witcher 3 Wild Hunt",
      createdAt = clock.nowUtc(),
      updatedAt = clock.nowUtc() + 2.hours
    )
    val nicolasCage = fakeNote(
      content = "# Nicolas Cage\nOur national treasure",
      createdAt = clock.nowUtc(),
      updatedAt = clock.nowUtc()
    )
    val uncharted = fakeNote(
      content = "# Uncharted\nThe Lost Legacy",
      folderId = archive.id
    )
    noteQueries.testInsert(nicolasCage, witcher3, uncharted)

    val models = presenter().uiModels()
      .map { it.rows }
      .test(rxRule)

    assertThat(models.popValue()).containsOnly(
      HomeUiModel.Folder(
        id = archive.id,
        title = "archive (folder)",
        subtitle = ""
      ),
      Note(
        id = nicolasCage.id,
        title = "Nicolas Cage",
        body = "Our national treasure"
      ),
      Note(
        id = witcher3.id,
        title = "",
        body = "The Witcher 3 Wild Hunt"
      )
    )
  }

  @Test fun `populate sub-folders and notes for a folder`() {
    val archive = fakeFolder("archive")
    val games = fakeFolder("games", parent = archive.id)
    folderQueries.insert(archive, games)

    val nicolasCage = fakeNote(
      content = "# Nicolas Cage\nOur national treasure",
      folderId = null
    )
    val witcher3 = fakeNote(
      content = "# The Witcher 3\nWild Hunt",
      folderId = archive.id
    )
    val uncharted = fakeNote(
      content = "# Uncharted\nThe Lost Legacy",
      folderId = games.id
    )
    noteQueries.testInsert(nicolasCage, witcher3, uncharted)

    val models = presenter(folder = archive.id).uiModels()
      .map { it.rows }
      .test(rxRule)

    assertThat(models.popValue()).containsOnly(
      HomeUiModel.Folder(
        id = games.id,
        title = "games (folder)",
        subtitle = ""
      ),
      Note(
        id = witcher3.id,
        title = "The Witcher 3",
        body = "Wild Hunt"
      ),
    )
  }

  @Test fun `filter out empty notes if requested`() {
    noteQueries.testInsert(
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
    noteQueries.testInsert(
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
    val presenter = presenter().also {
      it.uiModels().test()
    }
    assertThat(noteQueries.allNotes().executeAsList()).isEmpty()

    presenter.dispatch(NewNoteClicked)

    val savedNote = noteQueries.allNotes().executeAsOneOrNull()
    checkNotNull(savedNote)
    assertThat(navigator.pop()).isEqualTo(
      EditorScreenKey(NewNote(PreSavedNoteId(savedNote.id)))
    )
  }

  @Test fun `open new note screen on new-note keyboard shortcut`() {
    presenter().uiModels().test()
    assertThat(noteQueries.allNotes().executeAsList()).isEmpty()

    keyboardShortcuts.broadcast(KeyboardShortcuts.newNote)

    val savedNote = noteQueries.allNotes().executeAsOneOrNull()
    checkNotNull(savedNote)
    assertThat(navigator.pop()).isEqualTo(
      EditorScreenKey(NewNote(PreSavedNoteId(savedNote.id)))
    )
  }
}
