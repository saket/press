package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.node.Url
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.parser.node.url

class UrlVisitor : NodeVisitor<Url> {

  override fun visit(
    node: Url,
    writer: SpanWriter
  ) {
    writer.addForegroundColor(
        color = writer.style.link.urlColor,
        from = node.startOffset,
        to = node.endOffset
    )
    writer.addClickableUrl(
        url = node.url.toString(),
        from = node.startOffset,
        to = node.endOffset
    )
  }
}
