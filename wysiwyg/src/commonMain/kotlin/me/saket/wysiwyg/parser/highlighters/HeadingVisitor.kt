package me.saket.wysiwyg.parser.highlighters

import me.saket.wysiwyg.parser.MarkdownRenderer
import me.saket.wysiwyg.parser.node.Heading

@Suppress("SpellCheckingInspection")
class HeadingVisitor : SyntaxHighlighter<Heading> {

  override fun visitor(node: Heading): NodeVisitor<Heading>? {
    // Setext styles aren't supported. Setext-style headers are "underlined" using "="
    // (for first-level headers) and dashes (for second-level headers). For example:
    // This is an H1
    // =============
    //
    // This is an H2
    // -------------
    return when {
      node.isAtxHeading -> headingVisitor()
      else -> null
    }
  }

  // FYI compileKotlinMetadata task fails with an
  // error if the return type isn't explicitly specified.
  private fun headingVisitor(): NodeVisitor<Heading> =
    object : NodeVisitor<Heading> {
      override fun visit(
        node: Heading,
        renderer: MarkdownRenderer
      ) {
        renderer.addHeading(
          level = node.headingLevel,
          from = node.startOffset,
          to = node.endOffset
      )
      renderer.addForegroundColor(
            color = renderer.style.syntaxColor,
            from = node.startOffset,
          to = node.startOffset + node.openingMarker.length
      )
      renderer.addForegroundColor(
            color = renderer.style.heading.textColor,
            from = node.startOffset + node.openingMarker.length,
          to = node.endOffset
      )
    }
  }
}
