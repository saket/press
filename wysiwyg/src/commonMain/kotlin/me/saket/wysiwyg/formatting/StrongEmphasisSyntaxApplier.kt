package me.saket.wysiwyg.formatting

/**
 * Applies `**` markdown syntax to selected text or at the cursor position.
 */
object StrongEmphasisSyntaxApplier : MarkdownSyntaxApplier {

  private const val syntax = "**"

  override fun apply(text: String, selection: TextSelection): ApplyMarkdownSyntax {
    val selectedText = text.substring(selection.start, selection.end)
    val isTextSelected = selectedText.isNotEmpty()

    val newText = text.substring(0, selection.start) +
        syntax + selectedText + syntax +
        text.substring(selection.end, text.length)

    val newSelection = if (isTextSelected) {
      // Move to the end of the selected text.
      TextSelection.cursor(selection.end + syntax.length * 2)
    } else {
      // Move to the middle of the syntax.
      TextSelection.cursor(selection.cursorPosition + syntax.length)
    }

    return ApplyMarkdownSyntax(newText, newSelection)
  }
}
