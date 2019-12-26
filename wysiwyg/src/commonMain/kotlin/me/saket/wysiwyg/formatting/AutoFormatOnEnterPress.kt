package me.saket.wysiwyg.formatting

import me.saket.wysiwyg.util.isDigit
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Auto-inserts markdown syntaxes on enter:
 * - Fenced code blocks [```]
 * - List item [-, *, +, 1.]
 */
object AutoFormatOnEnterPress {

  private val formatters = listOf(StartFencedCodeBlock, ListContinuation)

  fun onEnter(textAfterEnter: String, cursorAfterEnter: TextSelection): ApplyMarkdownSyntax? {
    if (cursorAfterEnter.isCursor.not()) {
      return null
    }

    val cursorBeforeEnter = cursorAfterEnter.offsetBy(-1)

    // Paragraph under cursor.
    val paragraphBounds = ParagraphBounds.find(textAfterEnter, cursorBeforeEnter)
    val paragraph = textAfterEnter.substring(paragraphBounds.start, paragraphBounds.endExclusive)

    if (paragraph.isBlank()) {
      return null
    }

    return formatters
        .mapNotNull {
          it.onEnter(
              textAfterEnter,
              paragraph,
              paragraphBounds,
              cursorBeforeEnter,
              cursorAfterEnter
          )
        }
        .firstOrNull()
  }

  private interface OnEnterAutoFormatter {
    /**
     * @param paragraph Paragraph on which enter key was pressed.
     */
    fun onEnter(
      text: String,
      paragraph: String,
      paragraphBounds: ParagraphBounds,
      cursorBeforeEnter: TextSelection,
      cursorAfterEnter: TextSelection
    ): ApplyMarkdownSyntax?
  }

  private object StartFencedCodeBlock : OnEnterAutoFormatter {
    val fencedCodeRegex by lazy(NONE) { Regex("(```)[a-z]*[\\s\\S]*?(```)") }

    override fun onEnter(
      text: String,
      paragraph: String,
      paragraphBounds: ParagraphBounds,
      cursorBeforeEnter: TextSelection,
      cursorAfterEnter: TextSelection
    ): ApplyMarkdownSyntax? {
      if (!paragraph.startsWith("```")) {
        return null
      }

      val existingCodeBlocks = fencedCodeRegex.findAll(text)
      val cursorPositionBeforeEnter = cursorBeforeEnter.cursorPosition

      for (block in existingCodeBlocks) {
        // Check if the cursor already inside a code block.
        if (block.range.contains(cursorPositionBeforeEnter)) {
          return null
        }

        // Check if the cursor is placed after the closing syntax.
        val enterPressedOnClosingLine = paragraphBounds.start < block.range.last
            && cursorPositionBeforeEnter <= paragraphBounds.endExclusive

        if (enterPressedOnClosingLine) {
          // Cursor is on the same line as the closing
          // marker. This isn't a new code block.
          return null
        }
      }

      val cursorPositionAfterEnter = cursorAfterEnter.cursorPosition
      return ApplyMarkdownSyntax(
          newText = text.replaceRange(cursorPositionAfterEnter, cursorPositionAfterEnter, "\n```"),
          newSelection = cursorAfterEnter
      )
    }
  }

  private object ListContinuation : OnEnterAutoFormatter {
    private val orderedItemRegex by lazy(NONE) { Regex("(\\d+).\\s") }

    @Suppress("NAME_SHADOWING")
    override fun onEnter(
      text: String,
      paragraph: String,
      paragraphBounds: ParagraphBounds,
      cursorBeforeEnter: TextSelection,
      cursorAfterEnter: TextSelection
    ): ApplyMarkdownSyntax? {
      val paragraph = paragraph.trimStart()

      // Unordered list item.
      if (paragraph.length >= 2 && paragraph[0] in "*+-" && paragraph[1].isWhitespace()) {
        val isItemEmpty = paragraph.length == 2
        return if (isItemEmpty) {
          endListSyntax(text, paragraphBounds)
        } else {
          continueListSyntax(text, cursorAfterEnter, syntax = "${paragraph[0]} ")
        }
      }

      // Ordered list item.
      if (paragraph[0].isDigit()) {
        val matchResult = orderedItemRegex.find(paragraph)
        if (matchResult != null) {
          val (syntax, number) = matchResult.groupValues
          val isItemEmpty = paragraph.length == syntax.length

          return if (isItemEmpty) {
            endListSyntax(text, paragraphBounds)
          } else {
            val nextNumber = number.toInt().inc()
            continueListSyntax(text, cursorAfterEnter, syntax = "$nextNumber. ")
          }
        }
      }

      return null
    }

    private fun continueListSyntax(
      text: String,
      cursorAfterEnter: TextSelection,
      syntax: String
    ): ApplyMarkdownSyntax {
      return ApplyMarkdownSyntax(
          newText = text.insert(cursorAfterEnter.cursorPosition, syntax),
          newSelection = cursorAfterEnter.offsetBy(syntax.length)
      )
    }

    private fun endListSyntax(text: String, paragraphBounds: ParagraphBounds): ApplyMarkdownSyntax {
      return ApplyMarkdownSyntax(
          // Adding +1 to paragraph's end to consume the extra line break.
          newText = text.replaceRange(paragraphBounds.start, paragraphBounds.endExclusive + 1, ""),
          newSelection = TextSelection.cursor(paragraphBounds.start)
      )
    }
  }
}

private fun String.insert(index: Int, textToInsert: String): String {
  // TODO: use buildString() instead.
  return substring(0, index) + textToInsert + substring(index, length)
}

