package me.saket.wysiwyg.formatting

/**
 * Applies [```] markdown syntax to selected text or at the cursor position.
 */
object FencedCodeBlockSyntaxApplier : MarkdownSyntaxApplier {

  private const val syntax = "```"

  override fun apply(text: String, selection: TextSelection): ApplyMarkdownSyntax {
    val paragraphStart = findParagraphStart(text, selection)
    val paragraphEnd = findParagraphEnd(text, selection)
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

  private fun findParagraphStart(text: String, selection: TextSelection): Int {
    for (i in selection.start downTo 0) {
      if (i > 0 && text[i - 1] == '\n') {
        return i
      }
    }
    return 0
  }

  private fun findParagraphEnd(text: String, selection: TextSelection): Int {
    for (i in selection.end until text.length) {
      if (text[i] == '\n') {
        return i
      }
    }
    return text.length
  }
}
