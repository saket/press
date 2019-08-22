package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.parser.node.firstChild
import me.saket.wysiwyg.parser.node.nextNode
import me.saket.wysiwyg.spans.SpanPool

object RootNodeHighlighter : NodeVisitor<Node> {

  private val highlighters = SyntaxHighlighters()

  override fun visit(
    node: Node,
    pool: SpanPool,
    writer: SpanWriter
  ) {
    var child: Node? = node.firstChild

    while (child != null) {
      // A subclass of this visitor might modify the node, resulting in getNext returning a
      // different node or no node after visiting it. So get the next node before visiting.
      val next: Node? = child.nextNode

      val visitor = highlighters.nodeVisitor(child)
      visitor.visit(child, pool, writer)

      visit(child, pool, writer)
      child = next
    }
  }
}