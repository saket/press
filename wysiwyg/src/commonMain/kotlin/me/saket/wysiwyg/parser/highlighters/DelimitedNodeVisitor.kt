package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.DelimitedNode
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.parser.node.closingMarker
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.openingMarker
import me.saket.wysiwyg.parser.node.startOffset

abstract class DelimitedNodeVisitor<T> : NodeVisitor<T> where T : Node, T : DelimitedNode {

  override fun visit(
    node: T,
    writer: SpanWriter
  ) {
    highlightOpeningSyntax(node.openingMarker, node.startOffset, writer)
    highlightClosingSyntax(node.closingMarker, node.endOffset, writer)
  }

  companion object {
    fun highlightOpeningSyntax(
      openingMarker: CharSequence,
      startOffset: Int,
      writer: SpanWriter
    ) {
      if (openingMarker.isNotEmpty()) {
        writer.addForegroundColor(
            color = writer.style.syntaxColor,
            from = startOffset,
            to = startOffset + openingMarker.length
        )
      }
    }

    fun highlightClosingSyntax(
      closingMarker: CharSequence,
      endOffset: Int,
      writer: SpanWriter
    ) {
      if (closingMarker.isNotEmpty()) {
        writer.addForegroundColor(
            color = writer.style.syntaxColor,
            from = endOffset - closingMarker.length,
            to = endOffset
        )
      }
    }
  }
}
