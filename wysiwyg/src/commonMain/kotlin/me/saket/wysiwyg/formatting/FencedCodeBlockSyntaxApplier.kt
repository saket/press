package me.saket.wysiwyg.formatting

/**
 * Applies [```] markdown syntax to selected text or at the cursor position.
 */
object FencedCodeBlockSyntaxApplier : MarkdownSyntaxApplier {

  private const val leftSyntax = "```\n"
  private const val rightSyntax = "\n```"

  override fun apply(text: String, selection: TextSelection): ApplyMarkdownSyntax {
    val paraBounds = ParagraphBounds.find(text, selection)
    val paragraphUnderSelection = text.substring(paraBounds.start, paraBounds.endExclusive)

    return ApplyMarkdownSyntax(
      newText = text.substring(0, paraBounds.start)
        + leftSyntax + paragraphUnderSelection + rightSyntax
        + text.substring(paraBounds.endExclusive, text.length),
      newSelection = selection.copy(
        start = selection.start + leftSyntax.length,
        end = selection.end + leftSyntax.length
      )
    )
  }
}
