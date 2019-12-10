package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.RealSpanWriter
import me.saket.wysiwyg.parser.node.Strikethrough
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.foregroundColor
import me.saket.wysiwyg.spans.strikethrough

class StrikethroughVisitor : DelimitedNodeVisitor<Strikethrough>() {

  override fun visit(
    node: Strikethrough,
    pool: SpanPool,
    writer: RealSpanWriter
  ) {
    writer.add(pool.strikethrough(), node.startOffset, node.endOffset)
    writer.add(pool.foregroundColor(pool.style.strikethroughTextColor), node.startOffset, node.endOffset)
    super.visit(node, pool, writer)
  }
}
