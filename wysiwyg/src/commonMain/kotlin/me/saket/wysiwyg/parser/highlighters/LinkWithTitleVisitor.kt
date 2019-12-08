package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.Link
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.parser.node.text
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.foregroundColor

class LinkWithTitleVisitor : NodeVisitor<Link> {

  override fun visit(
    node: Link,
    pool: SpanPool,
    writer: SpanWriter
  ) {
    // Title.
    writer.add(pool.foregroundColor(pool.style.link.titleTextColor), node.startOffset, node.endOffset)

    // Url.
    val textClosingPosition = node.startOffset + node.text.length + 1
    val urlOpeningPosition = textClosingPosition + 1
    writer.add(
        pool.foregroundColor(pool.style.link.urlTextColor),
        urlOpeningPosition,
        node.endOffset
    )
  }
}
