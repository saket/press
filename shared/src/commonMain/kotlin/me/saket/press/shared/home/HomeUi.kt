package me.saket.press.shared.home

import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorPresenter
import me.saket.press.shared.ui.ScreenKey

interface HomeEvent {
  object NewNoteClicked : HomeEvent
  data class WindowFocusChanged(val hasFocus: Boolean) : HomeEvent
}

data class HomeUiModel(val notes: List<Note>) {
  data class Note(
    val noteId: NoteId,
    val adapterId: Long,
    val title: String,
    val body: String
  )
}

/**
 * @param noteId ID of empty note that should be shown in a
 * new screen. Although [EditorPresenter] is capable of starting
 * without any ID, this is easier to consume from SwiftUI.
 */
data class ComposeNewNote(val noteId: NoteId) : ScreenKey
