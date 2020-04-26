package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.MarkdownRenderer
import me.saket.wysiwyg.parser.node.StrongEmphasis
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset

class StrongEmphasisVisitor : DelimitedNodeVisitor<StrongEmphasis>() {

  override fun visit(
    node: StrongEmphasis,
    renderer: MarkdownRenderer
  ) {
    renderer.addBold(from = node.startOffset, to = node.endOffset)
    super.visit(node, renderer)
  }
}
