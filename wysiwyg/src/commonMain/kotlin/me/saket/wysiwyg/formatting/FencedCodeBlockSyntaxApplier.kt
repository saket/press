package me.saket.wysiwyg.formatting

/**
 * Applies [```] markdown syntax to selected text or at the cursor position.
 */
object FencedCodeBlockSyntaxApplier : MarkdownSyntaxApplier {

  private const val syntax = "```"

  override fun apply(text: String, selection: TextSelection): ApplyMarkdownSyntax {
    val (paragraphStart, paragraphEnd) = ParagraphBounds.find(text, selection.start)
    val paragraphUnderSelection = text.substring(paragraphStart, paragraphEnd)

    val leftSyntax = "$syntax\n"
    val rightSyntax = "\n$syntax"

    return ApplyMarkdownSyntax(
        newText = text.substring(0, paragraphStart)
            + leftSyntax + paragraphUnderSelection + rightSyntax
            + text.substring(paragraphEnd, text.length),
        newSelection = selection.copy(
            start = selection.start + leftSyntax.length,
            end = selection.end + leftSyntax.length
        )
    )
  }
}
