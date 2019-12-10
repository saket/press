package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.BulletList
import me.saket.wysiwyg.parser.node.ListBlock
import me.saket.wysiwyg.parser.node.OrderedList
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset

class OrderedListVisitor : ListBlockVisitor<OrderedList>()
class BulletListVisitor : ListBlockVisitor<BulletList>()

abstract class ListBlockVisitor<T : ListBlock> : NodeVisitor<T> {

  override fun visit(
    node: T,
    writer: SpanWriter
  ) {
    writer.addLeadingMargin(
        margin = writer.style.list.indentationMargin,
        from = node.startOffset,
        to = node.endOffset
    )
  }
}
