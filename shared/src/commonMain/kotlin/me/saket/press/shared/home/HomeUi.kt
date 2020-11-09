package me.saket.press.shared.home

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.ui.ScreenKey

@AndroidParcelize
class HomeScreenKey : ScreenKey

interface HomeEvent {
  object NewNoteClicked : HomeEvent
  data class WindowFocusChanged(val hasFocus: Boolean) : HomeEvent
}

data class HomeUiModel(val notes: List<Note>) {
  data class Note(
    val id: NoteId,
    val adapterId: Long,
    val title: String,
    val body: String
  )
}
