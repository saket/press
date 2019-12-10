package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.Code
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset

class InlineCodeVisitor : DelimitedNodeVisitor<Code>() {

  override fun visit(
    node: Code,
    writer: SpanWriter
  ) {
    writer.addInlineCode(from = node.startOffset, to = node.endOffset)
    writer.addMonospaceTypeface(from = node.startOffset, to = node.endOffset)
    super.visit(node, writer)
  }
}
