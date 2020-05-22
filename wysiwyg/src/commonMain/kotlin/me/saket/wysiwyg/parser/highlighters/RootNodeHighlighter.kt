package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.MarkdownRenderer
import me.saket.wysiwyg.parser.node.ListBlock
import me.saket.wysiwyg.parser.node.ListItem
import me.saket.wysiwyg.parser.node.Node

object RootNodeHighlighter : NodeVisitor<Node> {

  private val highlighters = SyntaxHighlighters()

  override fun visit(
    node: Node,
    renderer: MarkdownRenderer
  ) {
    var child: Node? = node.firstChild

    while (child != null) {
      // A subclass of this visitor might modify the node, resulting in getNext returning a
      // different node or no node after visiting it. So get the next node before visiting.
      val next: Node? = child.nextNode

      // Workaround for https://github.com/vsch/flexmark-java/issues/394.
      val isSubList = child is ListBlock && child.parent is ListItem
      if (isSubList.not()) {
        val visitor = highlighters.nodeVisitor(child)
        visitor.visit(child, renderer)
      }

      visit(child, renderer)
      child = next
    }
  }
}
