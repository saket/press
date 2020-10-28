package me.saket.wysiwyg.formatting

/**
 * For markdown syntaxes that use the same characters on both sides of text.
 * For example: **strong emphasis**, ~~strikethrough~~.
 */
abstract class InlineSymmetricMarkdownSyntaxApplier(private val syntax: String) : MarkdownSyntaxApplier {

  override fun apply(text: String, selection: TextSelection): ApplyMarkdownSyntax {
    val selectedText = text.substring(selection.start, selection.end)
    val isTextSelected = selectedText.isNotEmpty()

    val newText = text.substring(0, selection.start) +
      syntax + selectedText + syntax +
      text.substring(selection.end, text.length)

    val newSelection = if (isTextSelected) {
      // Preserve selection include the syntax.
      selection.copy(end = selection.end + syntax.length * 2)
    } else {
      // Move to the middle of the syntax.
      TextSelection.cursor(selection.cursorPosition + syntax.length)
    }

    return ApplyMarkdownSyntax(newText, newSelection)
  }
}
