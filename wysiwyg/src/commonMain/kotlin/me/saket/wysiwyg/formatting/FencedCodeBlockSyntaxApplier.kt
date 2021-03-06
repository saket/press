package me.saket.wysiwyg.formatting

/**
 * Applies [```] markdown syntax to selected text or at the cursor position.
 */
object FencedCodeBlockSyntaxApplier : MarkdownSyntaxApplier {

  private const val leftSyntax = "```\n"
  private const val rightSyntax = "\n```"

  override fun apply(text: CharSequence, selection: TextSelection): ReplaceTextWith {
    val paraBounds = ParagraphBounds.find(text, selection)
    val paragraphUnderSelection = text.substring(paraBounds.start, paraBounds.endExclusive)

    return ReplaceTextWith(
      replacement = text.replaceRange(
        startIndex = paraBounds.start,
        endIndex = paraBounds.endExclusive,
        replacement = "$leftSyntax$paragraphUnderSelection$rightSyntax"
      ),
      newSelection = selection.copy(
        start = selection.start + leftSyntax.length,
        end = selection.end + leftSyntax.length
      )
    )
  }
}
