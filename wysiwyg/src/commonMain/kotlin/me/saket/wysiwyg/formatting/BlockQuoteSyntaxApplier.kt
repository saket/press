package me.saket.wysiwyg.formatting

/**
 * Applies `>` markdown syntax to selected text or at the cursor position.
 */
object BlockQuoteSyntaxApplier : MarkdownSyntaxApplier {

  override fun apply(text: String, selection: TextSelection): ApplyMarkdownSyntax {
    val (paragraphStart, paragraphEnd) = ParagraphBounds.find(text, selection.start)
    val paragraphUnderSelection = text.substring(paragraphStart, paragraphEnd)

    // Nesting is when the same syntax is applied multiple times to the same paragraph.
    val isNesting = paragraphUnderSelection.getOrNull(0) == '>'
    val hasLeadingSpace = { paragraphUnderSelection.getOrNull(0)?.isWhitespace() ?: false }

    val syntax = when {
      isNesting || hasLeadingSpace() -> ">"
      else -> "> "
    }

    return ApplyMarkdownSyntax(
        newText = text.substring(0, paragraphStart)
            + syntax + paragraphUnderSelection
            + text.substring(paragraphEnd, text.length),
        newSelection = selection.copy(
            start = selection.start + syntax.length,
            end = selection.end + syntax.length
        )
    )
  }
}
