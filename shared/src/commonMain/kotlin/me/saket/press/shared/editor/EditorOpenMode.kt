package me.saket.press.shared.editor

import com.benasher44.uuid.Uuid

sealed class EditorOpenMode {
  // The base type intentionally does not have an abstract uuid
  // for discouraging usages to depend on it. The placeholder
  // uuid may or may not have been inserted into DB yet.

  data class NewNote(val placeholderUuid: Uuid) : EditorOpenMode()
  data class ExistingNote(val noteUuid: Uuid) : EditorOpenMode()
}
