package me.saket.wysiwyg.formatting

/**
 * Applies a markdown syntax to selected text or at the cursor position.
 * The can be applied to the same paragraph multiple times and this will
 * take care of combining multiple syntax markers without any whitespaces.
 */
abstract class CompoundableParagraphSyntaxApplier(
  private val leftSyntax: Char,
  private val addSurroundingLineBreaks: Boolean
) : MarkdownSyntaxApplier {

  override fun apply(text: String, selection: TextSelection): ApplyMarkdownSyntax {
    val paraBounds = ParagraphBounds.find(text, selection)
    val paragraphUnderSelection = text.substring(paraBounds.start, paraBounds.endExclusive)

    // Nesting is when the same syntax is applied multiple times to the same paragraph.
    val isNesting = paragraphUnderSelection.getOrNull(0) == leftSyntax
    val hasLeadingSpace = { paragraphUnderSelection.getOrNull(0)?.isWhitespace() ?: false }

    val compoundedLeftSyntax = when {
      isNesting || hasLeadingSpace() -> "$leftSyntax"
      else -> "$leftSyntax "
    }

    val needsLeadingNewLine = { paraBounds.start >= 2 && text[paraBounds.start - 2] != '\n' }
    val leadingNewLine = if (addSurroundingLineBreaks && needsLeadingNewLine()) "\n" else ""

    // text[paraBounds.endExclusive]     = \n character
    // text[paraBounds.endExclusive + 1] = if this is also \n then the paragraph has a line break.
    val needsFollowingNewLine = {
      val hasFollowingNewLine = text.getOrNull(paraBounds.endExclusive + 1) == '\n'
      paraBounds.endExclusive != text.length && hasFollowingNewLine.not()
    }
    val followingNewLine = if (addSurroundingLineBreaks && needsFollowingNewLine()) "\n" else ""

    return ApplyMarkdownSyntax(
      newText = text.substring(0, paraBounds.start)
        + leadingNewLine
        + compoundedLeftSyntax + paragraphUnderSelection
        + followingNewLine
        + text.substring(paraBounds.endExclusive, text.length),
      newSelection = selection.copy(
        start = selection.start + leadingNewLine.length + compoundedLeftSyntax.length,
        end = selection.end + leadingNewLine.length + compoundedLeftSyntax.length
      )
    )
  }
}
