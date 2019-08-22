package me.saket.wysiwyg.parser

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.widgets.EditableText

@Suppress("unused")
actual class MarkdownParser {

  private val parser: Parser = buildParser()

  actual fun parseSpans(text: String): Node {
    return parser.parse(text)
  }

  actual fun removeSpans(text: EditableText) {
    val spans = text.getSpans(0, text.length, Any::class.java)
    for (span in spans) {
      if (span is WysiwygSpan) {
        text.removeSpan(span)
        span.recycle()
      }
    }
  }

  private fun buildParser() = FlexmarkParserBuilder()
      .addExtension(StrikethroughExtension.create())
      .build()
}