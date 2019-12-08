package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.Url
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.foregroundColor

class UrlVisitor : NodeVisitor<Url> {

  override fun visit(
    node: Url,
    pool: SpanPool,
    writer: SpanWriter
  ) {
    writer.add(
        pool.foregroundColor(pool.style.link.urlTextColor),
        node.startOffset,
        node.endOffset
    )
  }
}
