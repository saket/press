package me.saket.wysiwyg.parser

import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.widgets.EditableText

actual class MarkdownParser {
  actual fun parseSpans(markdown: String): Node = TODO()

  /**
   * Called on every text change so that stale spans can
   * be removed before applying new ones.
   */
  actual fun removeSpans(text: EditableText): Unit = TODO()

  actual fun renderHtml(markdown: String): String = TODO()
}
