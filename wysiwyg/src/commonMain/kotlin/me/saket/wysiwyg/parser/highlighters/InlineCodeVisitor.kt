package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.MarkdownRenderer
import me.saket.wysiwyg.parser.node.Code
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset

class InlineCodeVisitor : DelimitedNodeVisitor<Code>() {

  override fun visit(
    node: Code,
    renderer: MarkdownRenderer
  ) {
    renderer.addInlineCode(from = node.startOffset, to = node.endOffset)
    renderer.addMonospaceTypeface(from = node.startOffset, to = node.endOffset)
    super.visit(node, renderer)
  }
}
