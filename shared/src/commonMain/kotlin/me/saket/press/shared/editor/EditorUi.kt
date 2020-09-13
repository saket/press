package me.saket.press.shared.editor

import me.saket.wysiwyg.formatting.TextSelection

// Specs for client apps to implement:
//
// - Send all EditorEvents.
//
// - Capitalize the first letter of a heading. See `CapitalizeOnHeadingStart`.
//
// - When a link is clicked, show a menu for letting the user actually open
//   the Url or place the cursor on the Url for editing it.
//
// - Remove rich text formatting when pasting text. See `WysiwygEditText`.
//   On Android, copying a URL will implicitly also copy its underline
//   span, which we don't want.
//
// - Call `AutoFormatOnEnterPress` when enter key is pressed for formatting markdown.
interface EditorEvent {
  data class NoteTextChanged(val text: String) : EditorEvent
}

data class EditorUiModel(
  val hintText: String?
)

sealed class EditorUiEffect {
  data class UpdateNoteText(
    val newText: String,
    val newSelection: TextSelection?
  ) : EditorUiEffect()

  object BlockedDueToSyncConflict: EditorUiEffect()
}
