package me.saket.press.shared.home

import com.benasher44.uuid.Uuid

interface HomeEvent {
  object NewNoteClicked : HomeEvent
}

data class HomeUiModel(val notes: List<Note>) {
  data class Note(
    val noteUuid: Uuid,
    val adapterId: Long,
    val title: String,
    val body: String
  )
}

sealed class HomeUiEffect {
  object ComposeNewNote: HomeUiEffect()
}
