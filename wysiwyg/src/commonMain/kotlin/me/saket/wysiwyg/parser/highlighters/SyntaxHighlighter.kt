package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.Node

interface SyntaxHighlighter<in T : Node> {
  fun visitor(node: T): NodeVisitor<T>?
}

interface NodeVisitor<in T : Node> {

  fun visit(
    node: T,
    writer: SpanWriter
  )

  companion object {
    val EMPTY = object : NodeVisitor<Node> {
      override fun visit(
        node: Node,
        writer: SpanWriter
      ) = Unit

      override fun toString() = "Empty node visitor"
    }
  }
}
