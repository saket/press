package me.saket.compose.shared.home

import com.benasher44.uuid.Uuid

data class HomeUiModel(val notes: List<Note>) {

  data class Note(
    val noteUuid: Uuid,
    val adapterId: Long,
    val title: String,
    val body: String
  )
}