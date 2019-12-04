package me.saket.press.shared.editor

data class EditorUiModel(
  val hintText: String?
)

sealed class EditorUiEffect {
  data class PopulateContent(val content: String) : EditorUiEffect()
  object CloseNote : EditorUiEffect()
}
