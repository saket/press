package me.saket.press.shared.editor

import me.saket.press.shared.AndroidParcel
import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER

sealed class EditorOpenMode: AndroidParcel {
  // The base type intentionally does not have an abstract uuid
  // for discouraging usages to depend on it. The placeholder
  // id may or may not have been inserted into DB yet.

  @AndroidParcelize
  data class NewNote(
    val placeholderId: NoteId,
    val preFilledNote: String?
  ) : EditorOpenMode()

  @AndroidParcelize
  data class ExistingNote(
    val noteId: NoteId
  ) : EditorOpenMode()

  fun showKeyboardOnStart() = when (this) {
    is NewNote -> preFilledNote.isNullOrBlank() || preFilledNote == NEW_NOTE_PLACEHOLDER
    is ExistingNote -> false
  }
}
