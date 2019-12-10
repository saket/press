package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.StrongEmphasis
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset

class StrongEmphasisVisitor : DelimitedNodeVisitor<StrongEmphasis>() {

  override fun visit(
    node: StrongEmphasis,
    writer: SpanWriter
  ) {
    writer.addBold(from = node.startOffset, to = node.endOffset)
    super.visit(node, writer)
  }
}
