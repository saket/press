package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.spans.SpanPool

interface SyntaxHighlighter<in T : Node> {
  fun visitor(node: T): NodeVisitor<T>?
}

interface NodeVisitor<in T : Node> {

  fun visit(
    node: T,
    pool: SpanPool,
    writer: SpanWriter
  )

  companion object {
    val EMPTY = object : NodeVisitor<Node> {
      override fun visit(
        node: Node,
        pool: SpanPool,
        writer: SpanWriter
      ) = Unit

      override fun toString() = "Empty node visitor"
    }
  }
}
