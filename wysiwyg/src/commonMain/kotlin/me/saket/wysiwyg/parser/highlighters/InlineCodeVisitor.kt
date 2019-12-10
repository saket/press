package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.RealSpanWriter
import me.saket.wysiwyg.parser.node.Code
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.inlineCode
import me.saket.wysiwyg.spans.monospaceTypeface

class InlineCodeVisitor : DelimitedNodeVisitor<Code>() {

  override fun visit(
    node: Code,
    pool: SpanPool,
    writer: RealSpanWriter
  ) {
    writer.add(pool.inlineCode(), node.startOffset, node.endOffset)
    writer.add(pool.monospaceTypeface(), node.startOffset, node.endOffset)
    super.visit(node, pool, writer)
  }
}
