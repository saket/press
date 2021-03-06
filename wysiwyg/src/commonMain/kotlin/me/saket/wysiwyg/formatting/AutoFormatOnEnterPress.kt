package me.saket.wysiwyg.formatting

import me.saket.wysiwyg.atomicLazy
import me.saket.wysiwyg.util.isDigit

object AutoFormatOnEnterPress {
  private val formatters = listOf(StartFencedCodeBlock, ListContinuation)

  /**
   * Auto-inserts markdown syntaxes on enter:
   * - Fenced code blocks [```]
   * - List item [-, *, +, 1.]
   *
   * @return New text for the note's content and a new cursor position.
   * Null if the text doesn't need to be formatted.
   */
  fun onEnter(textBeforeEnter: CharSequence, cursorBeforeEnter: TextSelection): ReplaceTextWith? {
    if (cursorBeforeEnter.isCursor.not()) {
      return null
    }

    // Paragraph under cursor.
    val paragraphBounds = ParagraphBounds.find(textBeforeEnter, cursorBeforeEnter)
    val paragraph = textBeforeEnter.subSequence(paragraphBounds.start, paragraphBounds.endExclusive)

    if (paragraph.isBlank()) {
      return null
    }

    return formatters
      .mapNotNull {
        it.onEnter(
          text = textBeforeEnter,
          paragraph = paragraph,
          paragraphBounds = paragraphBounds,
          cursorBeforeEnter = cursorBeforeEnter
        )
      }
      .firstOrNull()
  }

  private interface OnEnterAutoFormatter {
    /**
     * [text] is mutable so and shouldn't be modified. I decided against converting it to an immutable
     * String because it would discard all existing spans. Doing so on every key stroke may also be expensive.
     *
     * @param paragraph Paragraph on which enter key was pressed.
     */
    fun onEnter(
      text: CharSequence,
      paragraph: CharSequence,
      paragraphBounds: ParagraphBounds,
      cursorBeforeEnter: TextSelection
    ): ReplaceTextWith?
  }

  private object StartFencedCodeBlock : OnEnterAutoFormatter {
    val fencedCodeRegex by atomicLazy { Regex("(```)[a-z]*[\\s\\S]*?(```)") }

    override fun onEnter(
      text: CharSequence,
      paragraph: CharSequence,
      paragraphBounds: ParagraphBounds,
      cursorBeforeEnter: TextSelection
    ): ReplaceTextWith? {
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

      return ReplaceTextWith(
        replacement = text.insertAt(cursorBeforeEnter, "\n\n```"),
        newSelection = cursorBeforeEnter.offsetBy(1)
      )
    }
  }

  private object ListContinuation : OnEnterAutoFormatter {
    private val orderedItemRegex by atomicLazy { Regex("(\\d+)\\.\\s") }

    @Suppress("NAME_SHADOWING")
    override fun onEnter(
      text: CharSequence,
      paragraph: CharSequence,
      paragraphBounds: ParagraphBounds,
      cursorBeforeEnter: TextSelection
    ): ReplaceTextWith? {
      val paragraphMargin = paragraph.takeWhile { it.isWhitespace() }
      val paragraph = paragraph.trimStart()

      // Unordered list item.
      if (paragraph.length >= 2 && paragraph[0] in "*+-" && paragraph[1].isWhitespace()) {
        val isItemEmpty = paragraph.length == 2
        return if (isItemEmpty) {
          endListSyntax(text, cursorBeforeEnter, paragraphBounds)
        } else {
          continueListSyntax(text, cursorBeforeEnter, paragraphMargin, syntax = "${paragraph[0]} ")
        }
      }

      // Ordered list item.
      if (paragraph[0].isDigit()) {
        val matchResult = orderedItemRegex.find(paragraph)
        if (matchResult != null) {
          val (syntax, number) = matchResult.groupValues
          val isItemEmpty = paragraph.length == syntax.length

          return if (isItemEmpty) {
            endListSyntax(text, cursorBeforeEnter, paragraphBounds)
          } else {
            val nextNumber = number.toInt().inc()
            continueListSyntax(text, cursorBeforeEnter, paragraphMargin, syntax = "$nextNumber. ")
          }
        }
      }

      return null
    }

    private fun continueListSyntax(
      text: CharSequence,
      cursor: TextSelection,
      paragraphMargin: CharSequence,
      syntax: String
    ): ReplaceTextWith {
      val syntaxWithLineBreak = "\n$paragraphMargin$syntax"
      return ReplaceTextWith(
        replacement = text.insertAt(cursor, syntaxWithLineBreak),
        newSelection = cursor.offsetBy(syntaxWithLineBreak.length)
      )
    }

    private fun endListSyntax(text: CharSequence, cursor: TextSelection, lastItemBounds: ParagraphBounds): ReplaceTextWith {
      return ReplaceTextWith(
        replacement = text.removeRange(startIndex = lastItemBounds.start, endIndex = lastItemBounds.endExclusive),
        newSelection = cursor.offsetBy(-lastItemBounds.length)
      )
    }
  }
}

private fun CharSequence.insertAt(cursor: TextSelection, replacement: CharSequence): CharSequence {
  check(cursor.isCursor)
  return replaceRange(startIndex = cursor.cursorPosition, endIndex = cursor.cursorPosition, replacement)
}
