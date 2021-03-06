package me.saket.wysiwyg.formatting

/**
 * For markdown syntaxes that use the same characters on both sides of text.
 * For example: **strong emphasis**, ~~strikethrough~~.
 */
abstract class InlineSymmetricMarkdownSyntaxApplier(private val syntax: String) : MarkdownSyntaxApplier {

  override fun apply(text: CharSequence, selection: TextSelection): ReplaceTextWith {
    val selectedText = text.substring(selection.start, selection.end)
    val isTextSelected = selectedText.isNotEmpty()

    val newText = text.replaceRange(
      startIndex = selection.start,
      endIndex = selection.end,
      replacement = "$syntax$selectedText$syntax"
    )

    val newSelection = if (isTextSelected) {
      // Preserve selection when including the syntax.
      selection.copy(end = selection.end + syntax.length * 2)
    } else {
      // Move to the middle of the syntax.
      TextSelection.cursor(selection.cursorPosition + syntax.length)
    }

    return ReplaceTextWith(newText, newSelection)
  }
}
