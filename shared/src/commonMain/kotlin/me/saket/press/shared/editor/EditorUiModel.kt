package me.saket.press.shared.editor

interface EditorEvent {
  data class NoteTextChanged(val text: String) : EditorEvent
}

data class EditorUiModel(
  val hintText: String?
)

sealed class EditorUiEffect {
  data class PopulateContent(
    val content: String,
    val moveCursorToEnd: Boolean
  ) : EditorUiEffect()

  object CloseNote : EditorUiEffect()
}
