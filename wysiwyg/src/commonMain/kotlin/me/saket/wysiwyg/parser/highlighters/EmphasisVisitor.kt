package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.MarkdownRenderer
import me.saket.wysiwyg.parser.node.Emphasis
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset

class EmphasisVisitor : DelimitedNodeVisitor<Emphasis>() {

  override fun visit(
    node: Emphasis,
    renderer: MarkdownRenderer
  ) {
    renderer.addItalics(from = node.startOffset, to = node.endOffset)
    super.visit(node, renderer)
  }
}
