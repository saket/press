package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.Paragraph
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.paragraphBreak

class ParagraphVisitor : NodeVisitor<Paragraph> {

  override fun visit(
    node: Paragraph,
    pool: SpanPool,
    writer: SpanWriter
  ) {
    writer.add(pool.paragraphBreak(), node.startOffset, node.endOffset)
  }
}
