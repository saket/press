package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.BlockQuote
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.parent
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.foregroundColor
import me.saket.wysiwyg.spans.quote

class BlockQuoteVisitor : NodeVisitor<BlockQuote> {

  override fun visit(
    node: BlockQuote,
    pool: SpanPool,
    writer: SpanWriter
  ) {
    // Android requires quote spans to be inserted at the starting of the line. Nested
    // quote spans are otherwise not rendered correctly. Calculate the offset for this
    // quote's starting index instead and include all text from there under the spans.
    var nestedParents = 0
    var parent: Node? = node.parent
    while (parent is BlockQuote) {
      ++nestedParents
      parent = parent.parent
    }

    // Quote's vertical rule.
    val quoteSpan = pool.quote()
    writer.add(quoteSpan, node.startOffset - nestedParents, node.endOffset)

    // Quote markers ('>').
    val markerStartOffset = node.startOffset - nestedParents
    writer.add(pool.foregroundColor(pool.style.syntaxColor), markerStartOffset, node.startOffset + 1)

    // Text color.
    writer.add(
        pool.foregroundColor(pool.style.blockQuote.textColor), node.startOffset - nestedParents,
        node.endOffset
    )
  }
}
