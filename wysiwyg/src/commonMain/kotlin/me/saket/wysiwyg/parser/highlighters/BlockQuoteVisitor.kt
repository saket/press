package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.BlockQuote
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.parent
import me.saket.wysiwyg.parser.node.startOffset

class BlockQuoteVisitor : NodeVisitor<BlockQuote> {

  override fun visit(
    node: BlockQuote,
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

    // Quote's left border.
    writer.addQuote(
        from = node.startOffset - nestedParents,
        to = node.endOffset
    )

    // Quote markers ('>').
    writer.addForegroundColor(
        color = writer.style.syntaxColor,
        from = node.startOffset - nestedParents,
        to = node.startOffset + 1
    )

    // Text color.
    writer.addForegroundColor(
        color = writer.style.blockQuote.textColor,
        from = node.startOffset - nestedParents,
        to = node.endOffset
    )
  }
}
