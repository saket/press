package me.saket.compose.shared.editor

data class EditorUiModel(
  val hintText: String?
)

sealed class EditorUiUpdate {
  data class PopulateContent(val content: String) : EditorUiUpdate()
  object CloseNote : EditorUiUpdate()
}