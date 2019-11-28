package me.saket.wysiwyg.style

/**
 * Color palette and dimensions for highlighting markdown.
 */
data class WysiwygStyle(
  val syntaxColor: Int,
  val blockQuote: BlockQuote,
  val list: List,
  val link: Link,
  val thematicBreak: ThematicBreak,
  val code: Code,
  val heading: Heading
) {

  /** "> Quote" */
  data class BlockQuote(
    val leftBorderColor: Int,
    val leftBorderWidth: Int,
    val indentationMargin: Int,
    val textColor: Int
  )

  /** Ordered and unordered list. */
  data class List(
    /** Gap before a block of ordered/unordered list. */
    val indentationMargin: Int
  )

  /** "[Link title](https://domain.tld)" */
  data class Link(
    val titleTextColor: Int,
    val urlTextColor: Int
  )

  /** Thematic break a.k.a. horizontal rule. */
  data class ThematicBreak(
    val color: Int,
    val height: Float
  )

  /** Inline code and fenced code blocks. */
  data class Code(
    val backgroundColor: Int,
    val codeBlockMargin: Int
  )

  /** "# Heading" */
  data class Heading(
    val textColor: Int
  )

  companion object
}
