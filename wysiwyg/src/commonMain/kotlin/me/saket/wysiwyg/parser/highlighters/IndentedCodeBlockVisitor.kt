package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.IndentedCodeBlock
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.indentedCodeBlock
import me.saket.wysiwyg.spans.monospaceTypeface

class IndentedCodeBlockVisitor : NodeVisitor<IndentedCodeBlock> {

  override fun visit(
    node: IndentedCodeBlock,
    pool: SpanPool,
    writer: SpanWriter
  ) {
    // A LineBackgroundSpan needs to start at the starting of the line.
    val lineStartOffset = node.startOffset - 4

    writer.add(pool.indentedCodeBlock(), lineStartOffset, node.endOffset)
    writer.add(pool.monospaceTypeface(), node.startOffset, node.endOffset)
  }
}
