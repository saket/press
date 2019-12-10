package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.IndentedCodeBlock
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset

/**
 * TODO: Don't think Press supports indented code blocks. Verify and remove this.
 */
class IndentedCodeBlockVisitor : NodeVisitor<IndentedCodeBlock> {

  override fun visit(
    node: IndentedCodeBlock,
    writer: SpanWriter
  ) {
    // A LineBackgroundSpan needs to start at the starting of the line.
    val lineStartOffset = node.startOffset - 4

    writer.addIndentedCodeBlock(from = lineStartOffset, to = node.endOffset)
    writer.addMonospaceTypeface(from = node.startOffset, to = node.endOffset)
  }
}
