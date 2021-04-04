package me.saket.wysiwyg.style

import com.github.ajalt.colormath.RGB

/**
 * Color palette and dimensions for highlighting markdown.
 */
data class WysiwygStyle(
  val syntaxColor: Int,
  val strikethroughTextColor: Int,
  val blockQuote: BlockQuote,
  val code: Code,
  val heading: Heading,
  val link: Link,
  val list: List,
  val thematicBreak: ThematicBreak
) {

  init {
    check(RGB.fromInt(code.backgroundColor).alpha < 1f)
  }

  /** "> Quote" */
  data class BlockQuote(
    val leftBorderColor: Int,
    val leftBorderWidth: Int,
    val indentationMargin: Int,
    val textColor: Int
  )

  /**
   * Inline code and fenced code blocks.
   *
   * PSA: Use a translucent color on Android or else the cursor
   * will not show up because it's drawn behind the background.
   * */
  data class Code(
    val backgroundColor: Int,
    val codeBlockMargin: Int
  )

  /** "# Heading" */
  data class Heading(
    val textColor: Int
  )

  /** "[Link text](https://domain.tld)" */
  data class Link(
    val textColor: Int,
    val urlColor: Int
  )

  /** Ordered and unordered list. */
  data class List(
    /** Gap before a block of ordered/unordered list. */
    val indentationMargin: Int
  )

  /** Thematic break a.k.a. horizontal rule. */
  data class ThematicBreak(
    val color: Int,
    val height: Float
  )

  companion object
}
