package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.MarkdownRenderer
import me.saket.wysiwyg.parser.node.Url
import me.saket.wysiwyg.parser.node.endOffset
import me.saket.wysiwyg.parser.node.startOffset
import me.saket.wysiwyg.parser.node.url

class UrlVisitor : NodeVisitor<Url> {

  override fun visit(
    node: Url,
    renderer: MarkdownRenderer
  ) {
    renderer.addForegroundColor(
      color = renderer.style.link.urlColor,
      from = node.startOffset,
      to = node.endOffset
    )
    renderer.addClickableUrl(
      url = node.url.toString(),
      from = node.startOffset,
      to = node.endOffset
    )
  }
}
