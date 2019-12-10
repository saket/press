package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.RealSpanWriter
import me.saket.wysiwyg.parser.node.LinkWithTitle
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.parser.node.text
import me.saket.wysiwyg.parser.node.url
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.clickableUrl
import me.saket.wysiwyg.spans.foregroundColor

class LinkWithTitleVisitor : NodeVisitor<LinkWithTitle> {

  override fun visit(
    node: LinkWithTitle,
    pool: SpanPool,
    writer: RealSpanWriter
  ) {
    // Text.
    writer.add(pool.foregroundColor(pool.style.link.textColor), node.startOffset, node.endOffset)

    // Url.
    val textClosingPosition = node.startOffset + node.text.length + 1
    val urlOpeningPosition = textClosingPosition + 1
    writer.add(
        pool.foregroundColor(pool.style.link.urlColor),
        urlOpeningPosition,
        node.endOffset
    )
    writer.add(pool.clickableUrl(node.url.toString()), urlOpeningPosition, node.endOffset)
  }
}
