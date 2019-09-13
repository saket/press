package me.saket.compose.shared.editor

import com.benasher44.uuid.Uuid

sealed class EditorOpenMode {
  abstract val noteUuid: Uuid

  data class NewNote(override val noteUuid: Uuid) : EditorOpenMode()
  data class ExistingNote(override val noteUuid: Uuid) : EditorOpenMode()
}