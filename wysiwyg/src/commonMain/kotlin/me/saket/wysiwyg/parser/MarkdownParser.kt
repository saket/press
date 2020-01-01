package me.saket.wysiwyg.parser

import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.widgets.EditableText

expect class MarkdownParser() {

  fun parseSpans(text: String): Node

  /**
   * Called on every text change so that stale spans can
   * be removed before applying new ones.
   */
  fun removeSpans(text: EditableText)
}
