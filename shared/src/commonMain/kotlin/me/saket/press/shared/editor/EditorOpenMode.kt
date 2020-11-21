package me.saket.press.shared.editor

import me.saket.press.shared.AndroidParcel
import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.db.NoteId

sealed class EditorOpenMode : AndroidParcel {
  abstract val noteId: NoteIdKind

  /**
   * Opened by pressing the (+) FAB (Floating Action Button) in home screen.
   */
  @AndroidParcelize
  data class NewNote(
    override val noteId: NoteIdKind,
    val preFilledNote: String? = null
  ) : EditorOpenMode()

  /**
   * Opened by clicking a note in the note list.
   */
  @AndroidParcelize
  data class ExistingNote(
    override val noteId: PreSavedNoteId
  ) : EditorOpenMode() {
    constructor(noteId: NoteId) : this(PreSavedNoteId(noteId))
  }
}

// The base type intentionally does not have an abstract uuid
// for discouraging usages to depend on it. The placeholder
// id may or may not have been inserted into DB yet.
sealed class NoteIdKind : AndroidParcel

@AndroidParcelize
data class PlaceholderNoteId(val id: NoteId) : NoteIdKind()

// TODO: rename to ExistingNoteId
@AndroidParcelize
data class PreSavedNoteId(val id: NoteId) : NoteIdKind()
