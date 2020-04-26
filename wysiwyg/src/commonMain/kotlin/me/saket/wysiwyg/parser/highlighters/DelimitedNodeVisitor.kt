package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.MarkdownRenderer
import me.saket.wysiwyg.parser.node.DelimitedNode
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.parser.node.closingMarker
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.openingMarker
import me.saket.wysiwyg.parser.node.startOffset

abstract class DelimitedNodeVisitor<T> : NodeVisitor<T> where T : Node, T : DelimitedNode {

  override fun visit(
    node: T,
    renderer: MarkdownRenderer
  ) {
    highlightOpeningSyntax(node.openingMarker, node.startOffset, renderer)
    highlightClosingSyntax(node.closingMarker, node.endOffset, renderer)
  }

  companion object {
    fun highlightOpeningSyntax(
      openingMarker: CharSequence,
      startOffset: Int,
      renderer: MarkdownRenderer
    ) {
      if (openingMarker.isNotEmpty()) {
        renderer.addForegroundColor(
            color = renderer.style.syntaxColor,
            from = startOffset,
            to = startOffset + openingMarker.length
        )
      }
    }

    fun highlightClosingSyntax(
      closingMarker: CharSequence,
      endOffset: Int,
      renderer: MarkdownRenderer
    ) {
      if (closingMarker.isNotEmpty()) {
        renderer.addForegroundColor(
            color = renderer.style.syntaxColor,
            from = endOffset - closingMarker.length,
            to = endOffset
        )
      }
    }
  }
}
