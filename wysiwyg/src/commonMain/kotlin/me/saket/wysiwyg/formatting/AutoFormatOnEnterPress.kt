package me.saket.wysiwyg.formatting

/**
 * Auto-inserts markdown syntaxes on enter:
 * - Fenced code blocks [```]
 * - List item (TODO)
 */
object AutoFormatOnEnterPress {

  private val formatters = listOf<OnEnterAutoFormatter>(FencedCodeBlock)

  fun onEnter(text: String, selection: TextSelection): ApplyMarkdownSyntax? {
    if (selection.isCursor.not()) {
      return null
    }

    val paragraphBounds = ParagraphBounds.find(text, selection)
    val paragraphUnderSelection = text.substring(paragraphBounds.start, paragraphBounds.endExclusive)

    return formatters
        .mapNotNull { it.onEnter(text, paragraphUnderSelection, selection) }
        .firstOrNull()
  }

  private interface OnEnterAutoFormatter {
    /**
     * @param paragraph Paragraph under the cursor before enter key was pressed.
     * @param selection cursor position before enter key was pressed.
     */
    fun onEnter(text: String, paragraph: String, selection: TextSelection): ApplyMarkdownSyntax?
  }

  private object FencedCodeBlock : OnEnterAutoFormatter {
    val fencedCodeRegex = Regex("```[a-z]*[\\s\\S]*?```")

    override fun onEnter(text: String, paragraph: String, selection: TextSelection): ApplyMarkdownSyntax? {
      // TODO: consume only if cursor isn't already inside a fenced code block.
      val isCodeBlock = paragraph.startsWith("```")
      if (!isCodeBlock) {
        return null
      }

      val cursor = selection.cursorPosition
      val isAlreadyInsideACodeBlock = fencedCodeRegex
          .findAll(text)
          .any { it.range.contains(cursor) }

      if (isAlreadyInsideACodeBlock) {
        return null
      }

      return ApplyMarkdownSyntax(
          newText = text.substring(0, cursor) + "\n\n```" + text.substring(cursor),
          newSelection = selection.offsetBy(1)
      )
    }
  }
}
