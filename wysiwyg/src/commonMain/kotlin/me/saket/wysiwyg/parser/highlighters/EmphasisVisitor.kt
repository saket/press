package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.Emphasis
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.italics

class EmphasisVisitor : DelimitedNodeVisitor<Emphasis>() {

  override fun visit(
    node: Emphasis,
    pool: SpanPool,
    writer: SpanWriter
  ) {
    writer.add(pool.italics(), node.startOffset, node.endOffset)
    super.visit(node, pool, writer)
  }
}
