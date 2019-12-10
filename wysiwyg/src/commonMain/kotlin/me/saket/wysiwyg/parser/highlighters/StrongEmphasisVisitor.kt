package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.RealSpanWriter
import me.saket.wysiwyg.parser.node.StrongEmphasis
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.bold

class StrongEmphasisVisitor : DelimitedNodeVisitor<StrongEmphasis>() {

  override fun visit(
    node: StrongEmphasis,
    pool: SpanPool,
    writer: RealSpanWriter
  ) {
    writer.add(pool.bold(), node.startOffset, node.endOffset)
    super.visit(node, pool, writer)
  }
}
