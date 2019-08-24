package me.saket.wysiwyg

expect class WysiwygTheme {
  val syntaxColor: Int
  val blockQuoteVerticalRuleColor: Int
  val blockQuoteTextColor: Int
  val blockQuoteVerticalRuleStrokeWidth: Int
  val blockQuoteIndentationMargin: Int
  val listBlockIndentationMargin: Int
  val linkUrlColor: Int
  val linkTextColor: Int
  val thematicBreakColor: Int
  val thematicBreakThickness: Float
  val codeBackgroundColor: Int
  val codeBlockMargin: Int
}