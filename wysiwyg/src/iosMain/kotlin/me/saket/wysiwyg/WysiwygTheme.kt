package me.saket.wysiwyg

actual data class WysiwygTheme(
  actual val syntaxColor: Int,
  actual val blockQuoteVerticalRuleColor: Int,
  actual val blockQuoteTextColor: Int,
  actual val blockQuoteVerticalRuleStrokeWidth: Int,
  actual val blockQuoteIndentationMargin: Int,
  actual val listBlockIndentationMargin: Int,
  actual val linkUrlColor: Int,
  actual val linkTextColor: Int,
  actual val thematicBreakColor: Int,
  actual val thematicBreakThickness: Float,
  actual val codeBackgroundColor: Int,
  actual val codeBlockMargin: Int
)