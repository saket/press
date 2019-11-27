package me.saket.wysiwyg.theme

data class WysiwygTheme(

  /** Used for resolving default dimensions. */
  val displayUnits: DisplayUnits,

  /** Color used for highlighting '**', '~~' and other syntax characters. */
  val syntaxColor: Int,

  /** Color a '>' blockquote's leading margin. */
  val blockQuoteVerticalRuleColor: Int,

  val blockQuoteTextColor: Int,

  /** Width of a block-quote's vertical line/stripe/rule. */
  val blockQuoteVerticalRuleStrokeWidth: Int = displayUnits.fromPixels(4).toInt(),

  /** Gap before a block-quote. */
  val blockQuoteIndentationMargin: Int = displayUnits.fromPixels(24).toInt(),

  /** Gap before a block of ordered/unordered list. */
  val listBlockIndentationMargin: Int = displayUnits.fromPixels(24).toInt(),

  val linkTextColor: Int,

  val linkUrlColor: Int,

  /** Thematic break a.k.a. horizontal rule. */
  val thematicBreakColor: Int,

  val thematicBreakThickness: Float = displayUnits.fromPixels(4),

  val codeBackgroundColor: Int,

  val codeBlockMargin: Int = displayUnits.fromPixels(8).toInt(),

  val headingTextColor: Int
)

