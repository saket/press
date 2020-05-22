package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.MarkdownRenderer
import me.saket.wysiwyg.parser.node.LinkWithTitle

class LinkWithTitleVisitor : NodeVisitor<LinkWithTitle> {

  override fun visit(
    node: LinkWithTitle,
    renderer: MarkdownRenderer
  ) {
    // Text.
    renderer.addForegroundColor(
        color = renderer.style.link.textColor,
        from = node.startOffset,
        to = node.endOffset
    )

    // Url.
    val textClosingPosition = node.startOffset + node.text.length + 1
    val urlOpeningPosition = textClosingPosition + 1
    renderer.addForegroundColor(
        color = renderer.style.link.urlColor,
        from = urlOpeningPosition,
        to = node.endOffset
    )
    renderer.addClickableUrl(
        url = node.url.toString(),
        from = urlOpeningPosition,
        to = node.endOffset
    )
  }
}
