package me.saket.press.shared.home

import me.saket.press.shared.db.NoteId
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
 * @param newNoteId ID that should be used for creating a new note.
 */
data class ComposeNewNote(val newNoteId: NoteId) : ScreenKey
