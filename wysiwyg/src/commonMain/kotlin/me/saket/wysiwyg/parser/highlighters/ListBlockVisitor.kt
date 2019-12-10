package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.RealSpanWriter
import me.saket.wysiwyg.parser.node.BulletList
import me.saket.wysiwyg.parser.node.ListBlock
import me.saket.wysiwyg.parser.node.OrderedList
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.leadingMargin

class OrderedListVisitor : ListBlockVisitor<OrderedList>()
class BulletListVisitor : ListBlockVisitor<BulletList>()

abstract class ListBlockVisitor<T : ListBlock> : NodeVisitor<T> {

  override fun visit(
    node: T,
    pool: SpanPool,
    writer: RealSpanWriter
  ) {
    val marginSpan = pool.leadingMargin(pool.style.list.indentationMargin)
    writer.add(marginSpan, node.startOffset, node.endOffset)
  }
}
