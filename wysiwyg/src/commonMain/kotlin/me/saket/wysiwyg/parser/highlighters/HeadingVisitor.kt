package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.Heading
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.headingLevel
import me.saket.wysiwyg.parser.node.isAtxHeading
import me.saket.wysiwyg.parser.node.openingMarker
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.foregroundColor
import me.saket.wysiwyg.spans.heading

@Suppress("SpellCheckingInspection")
class HeadingVisitor : SyntaxHighlighter<Heading> {

  override fun visitor(node: Heading): NodeVisitor<Heading>? {
    // Setext styles aren't supported. Setext-style headers are "underlined" using "="
    // (for first-level headers) and dashes (for second-level headers). For example:
    // This is an H1
    // =============
    //
    // This is an H2
    // -------------
    return when {
      node.isAtxHeading -> headingVisitor()
      else -> null
    }
  }

  private fun headingVisitor() = object : NodeVisitor<Heading> {
    override fun visit(
      node: Heading,
      pool: SpanPool,
      writer: SpanWriter
    ) {
      writer.add(pool.heading(node.headingLevel), node.startOffset, node.endOffset)
      writer.add(
          pool.foregroundColor(pool.style.syntaxColor),
          node.startOffset,
          node.startOffset + node.openingMarker.length
      )
      writer.add(
          pool.foregroundColor(pool.style.heading.textColor),
          node.startOffset + node.openingMarker.length,
          node.endOffset
      )
    }
  }
}
