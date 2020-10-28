package me.saket.wysiwyg.parser

import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet
import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.widgets.EditableText

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

  private fun buildParser(): Parser {
    val options = MutableDataSet().apply {
      set(Parser.HTML_BLOCK_PARSER, false)
      set(Parser.INDENTED_CODE_BLOCK_PARSER, false)
    }

    return Parser.builder(options)
      .extensions(
        listOf(
          StrikethroughExtension.create(),
          AutolinkExtension.create()
        )
      )
      .build()
  }
}
