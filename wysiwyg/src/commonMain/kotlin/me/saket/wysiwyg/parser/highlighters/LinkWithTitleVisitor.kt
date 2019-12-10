package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.LinkWithTitle
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.parser.node.text
import me.saket.wysiwyg.parser.node.url

class LinkWithTitleVisitor : NodeVisitor<LinkWithTitle> {

  override fun visit(
    node: LinkWithTitle,
    writer: SpanWriter
  ) {
    // Text.
    writer.addForegroundColor(
        color = writer.style.link.textColor,
        from = node.startOffset,
        to = node.endOffset
    )

    // Url.
    val textClosingPosition = node.startOffset + node.text.length + 1
    val urlOpeningPosition = textClosingPosition + 1
    writer.addForegroundColor(
        color = writer.style.link.urlColor,
        from = urlOpeningPosition,
        to = node.endOffset
    )
    writer.addClickableUrl(
        url = node.url.toString(),
        from = urlOpeningPosition,
        to = node.endOffset
    )
  }
}
