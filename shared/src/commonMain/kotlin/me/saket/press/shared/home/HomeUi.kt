package me.saket.press.shared.home

import me.saket.press.shared.db.NoteId

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

sealed class HomeUiEffect {
  object ComposeNewNote : HomeUiEffect()
}
