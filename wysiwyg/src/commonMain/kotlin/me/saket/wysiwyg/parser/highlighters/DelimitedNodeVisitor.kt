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
    highlightOpeningSyntax(node.openingMarker, node.startOffset, writer, pool)
    highlightClosingSyntax(node.closingMarker, node.endOffset, writer, pool)
  }

  companion object {

    fun highlightOpeningSyntax(
      openingMarker: CharSequence,
      startOffset: Int,
      writer: SpanWriter,
      pool: SpanPool
    ) {
      if (openingMarker.isNotEmpty()) {
        writer.add(
            pool.foregroundColor(pool.style.syntaxColor),
            startOffset,
            startOffset + openingMarker.length
        )
      }
    }

    fun highlightClosingSyntax(
      closingMarker: CharSequence,
      endOffset: Int,
      writer: SpanWriter,
      pool: SpanPool
    ) {
      if (closingMarker.isNotEmpty()) {
        writer.add(
            pool.foregroundColor(pool.style.syntaxColor),
            endOffset - closingMarker.length,
            endOffset
        )
      }
    }
  }
}
