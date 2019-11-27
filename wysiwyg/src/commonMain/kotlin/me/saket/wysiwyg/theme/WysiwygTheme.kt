package me.saket.wysiwyg.theme

data class WysiwygTheme(

  /** Used for resolving default dimensions. */
  val displayUnits: DisplayUnits,

  /** Color used for highlighting '**', '~~' and other syntax characters. */
  val syntaxColor: Int = "#CCAEF9".toHexColor(),

  val blockQuoteVerticalRuleColor: Int = "#CCAEF9".toHexColor(),

  val blockQuoteTextColor: Int = "#CED2F8".toHexColor(),

  /** Width of a block-quote's vertical line/stripe/rule. */
  val blockQuoteVerticalRuleStrokeWidth: Int = displayUnits.fromPixels(4).toInt(),

  /** Gap before a block-quote. */
  val blockQuoteIndentationMargin: Int = displayUnits.fromPixels(24).toInt(),

  /** Gap before a block of ordered/unordered list. */
  val listBlockIndentationMargin: Int = displayUnits.fromPixels(24).toInt(),

  // A translucent color ensures that the text remains legible even on a lighter background.
  val linkUrlColor: Int = "#AAC6D1FF".toHexColor(),

  val linkTextColor: Int = "#8DF0FF".toHexColor(),

  /** Thematic break a.k.a. horizontal rule. */
  val thematicBreakColor: Int = "#62677C".toHexColor(),

  val thematicBreakThickness: Float = displayUnits.fromPixels(4),

  val codeBackgroundColor: Int = "#242632".toHexColor(),

  val codeBlockMargin: Int = displayUnits.fromPixels(8).toInt(),

  val headingTextColor: Int = "#50FA7B".toHexColor()
)

