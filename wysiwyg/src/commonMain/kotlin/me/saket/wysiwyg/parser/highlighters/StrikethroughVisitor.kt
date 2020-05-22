package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.MarkdownRenderer
import me.saket.wysiwyg.parser.node.Strikethrough

class StrikethroughVisitor : DelimitedNodeVisitor<Strikethrough>() {

  override fun visit(
    node: Strikethrough,
    renderer: MarkdownRenderer
  ) {
    renderer.addStrikethrough(
        from = node.startOffset,
        to = node.endOffset
    )

    renderer.addForegroundColor(
        color = renderer.style.strikethroughTextColor,
        from = node.startOffset + node.openingMarker.length,
        to = node.endOffset - node.closingMarker.length
    )
    super.visit(node, renderer)
  }
}
