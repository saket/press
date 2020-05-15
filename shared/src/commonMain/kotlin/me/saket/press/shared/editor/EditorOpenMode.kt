package me.saket.press.shared.editor

import me.saket.press.shared.db.NoteId

sealed class EditorOpenMode {
  // The base type intentionally does not have an abstract uuid
  // for discouraging usages to depend on it. The placeholder
  // id may or may not have been inserted into DB yet.

  data class NewNote(val placeholderId: NoteId, val preFilledNote: String? = null) : EditorOpenMode()
  data class ExistingNote(val noteId: NoteId) : EditorOpenMode()

  fun showKeyboardOnStart() = when (this) {
    is NewNote -> preFilledNote.isNullOrBlank() || preFilledNote == EditorPresenter.NEW_NOTE_PLACEHOLDER
    is ExistingNote -> false
  }
}
