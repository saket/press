package me.saket.wysiwyg.formatting

/**
 * Applies a markdown syntax to selected text or at the cursor position.
 * The can be applied to the same paragraph multiple times and this will
 * take care of combining multiple syntax markers without any whitespaces.
 */
abstract class CompoundableParagraphSyntaxApplier(private val leftSyntax: Char) : MarkdownSyntaxApplier {

  override fun apply(text: String, selection: TextSelection): ApplyMarkdownSyntax {
    val (paragraphStart, paragraphEnd) = ParagraphBounds.find(text, selection.start)
    val paragraphUnderSelection = text.substring(paragraphStart, paragraphEnd)

    // Nesting is when the same syntax is applied multiple times to the same paragraph.
    val isNesting = paragraphUnderSelection.getOrNull(0) == leftSyntax
    val hasLeadingSpace = { paragraphUnderSelection.getOrNull(0)?.isWhitespace() ?: false }

    val compoundedLeftSyntax = when {
      isNesting || hasLeadingSpace() -> "$leftSyntax"
      else -> "$leftSyntax "
    }

    return ApplyMarkdownSyntax(
        newText = text.substring(0, paragraphStart)
            + compoundedLeftSyntax + paragraphUnderSelection
            + text.substring(paragraphEnd, text.length),
        newSelection = selection.copy(
            start = selection.start + compoundedLeftSyntax.length,
            end = selection.end + compoundedLeftSyntax.length
        )
    )
  }
}
