package me.saket.press.shared.theme

abstract class ThemePalette(
  val primaryColor: Int,
  val primaryColorDark: Int,
  val accentColor: Int,
  val textHighlightColor: Int,
  val window: WindowPalette,
  val markdown: MarkdownPalette,
  val textColorHeading: Int,
  val textColorPrimary: Int,
  val textColorSecondary: Int,
  val fabColor: Int
) {
  data class WindowPalette(
    val backgroundColor: Int,
    val editorBackgroundColor: Int
  )
  data class MarkdownPalette(
    val heading: Int,
    val syntaxColor: Int,
    val blockQuoteVerticalRuleColor: Int,
    val blockQuoteTextColor: Int,
    val linkUrlColor: Int,
    val linkTextColor: Int,
    val thematicBreakColor: Int,
    val codeBackgroundColor: Int,
    val headingTextColor: Int
  )
}
