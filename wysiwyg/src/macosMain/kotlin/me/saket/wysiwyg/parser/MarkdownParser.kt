package me.saket.wysiwyg.parser

import cmark.*
import kotlinx.cinterop.*
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.widgets.EditableText

actual class MarkdownParser {

  actual fun parseSpans(text: String): Node {
    val document: CPointer<cmark_node>? = cmark_parse_document(text, text.length.convert(), CMARK_OPT_DEFAULT)
    println("Parsed document: $document")

    val iterator: CPointer<cmark_iter>? = cmark_iter_new(root = document)

    while (true) {
      val eventType: cmark_event_type = cmark_iter_next(iterator)
      if (eventType == cmark_event_type.CMARK_EVENT_DONE) {
        break
      }

      val node: CPointer<cmark_node>? = cmark_iter_get_node(iterator)
      val nodeType: CPointer<ByteVarOf<Byte>> = cmark_node_get_type_string(node)!!
      println("nodeType: ${nodeType.toKString()}")
      cmark_node_free(node)
    }

    cmark_iter_free(iterator)
    cmark_node_free(document)

    TODO()
  }

  /**
   * Called on every text change so that stale spans can
   * be removed before applying new ones.
   */
  actual fun removeSpans(text: EditableText): Unit = TODO()
}
