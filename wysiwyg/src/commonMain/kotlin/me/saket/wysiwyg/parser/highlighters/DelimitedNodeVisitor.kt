package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.DelimitedNode
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.parser.node.closingMarker
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.openingMarker
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.foregroundColor

abstract class DelimitedNodeVisitor<T>
  : NodeVisitor<T> where T : Node, T : DelimitedNode {

  override fun visit(
    node: T,
    pool: SpanPool,
    writer: SpanWriter
  ) {
    if (node.openingMarker.isNotEmpty()) {
      writer.add(
          pool.foregroundColor(pool.theme.syntaxColor),
          node.startOffset,
          node.startOffset + node.openingMarker.length
      )
    }

    if (node.closingMarker.isNotEmpty()) {
      writer.add(
          pool.foregroundColor(pool.theme.syntaxColor),
          node.endOffset - node.closingMarker.length,
          node.endOffset
      )
    }
  }
}