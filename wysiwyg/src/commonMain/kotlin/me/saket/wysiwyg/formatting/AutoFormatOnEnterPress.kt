package me.saket.wysiwyg.formatting

import me.saket.wysiwyg.formatting.ReplaceNewLineWith.DeleteLetters
import me.saket.wysiwyg.formatting.ReplaceNewLineWith.InsertLetters
import me.saket.wysiwyg.util.isDigit
import kotlin.LazyThreadSafetyMode.NONE

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
  fun onEnter(textBeforeEnter: CharSequence, cursorBeforeEnter: TextSelection): ReplaceNewLineWith? {
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
              textBeforeEnter,
              paragraph,
              paragraphBounds,
              cursorBeforeEnter
          )
        }
        .firstOrNull()
  }

  private interface OnEnterAutoFormatter {
    /**
     * @param paragraph Paragraph on which enter key was pressed.
     */
    fun onEnter(
      text: CharSequence,
      paragraph: CharSequence,
      paragraphBounds: ParagraphBounds,
      cursorBeforeEnter: TextSelection
    ): ReplaceNewLineWith?
  }

  private object StartFencedCodeBlock : OnEnterAutoFormatter {
    val fencedCodeRegex by lazy(NONE) { Regex("(```)[a-z]*[\\s\\S]*?(```)") }

    override fun onEnter(
      text: CharSequence,
      paragraph: CharSequence,
      paragraphBounds: ParagraphBounds,
      cursorBeforeEnter: TextSelection
    ): ReplaceNewLineWith? {
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

      return InsertLetters(
          replacement = "\n\n```",
          newSelection = cursorBeforeEnter.offsetBy(1)
      )
    }
  }

  private object ListContinuation : OnEnterAutoFormatter {
    private val orderedItemRegex by lazy(NONE) { Regex("(\\d+)\\.\\s") }

    @Suppress("NAME_SHADOWING")
    override fun onEnter(
      text: CharSequence,
      paragraph: CharSequence,
      paragraphBounds: ParagraphBounds,
      cursorBeforeEnter: TextSelection
    ): ReplaceNewLineWith? {
      val paragraphMargin = paragraph.takeWhile { it.isWhitespace() }
      val paragraph = paragraph.trimStart()

      // Unordered list item.
      if (paragraph.length >= 2 && paragraph[0] in "*+-" && paragraph[1].isWhitespace()) {
        val isItemEmpty = paragraph.length == 2
        return if (isItemEmpty) {
          endListSyntax(paragraphBounds)
        } else {
          continueListSyntax(cursorBeforeEnter, paragraphMargin, syntax = "${paragraph[0]} ")
        }
      }

      // Ordered list item.
      if (paragraph[0].isDigit()) {
        val matchResult = orderedItemRegex.find(paragraph)
        if (matchResult != null) {
          val (syntax, number) = matchResult.groupValues
          val isItemEmpty = paragraph.length == syntax.length

          return if (isItemEmpty) {
            endListSyntax(paragraphBounds)
          } else {
            val nextNumber = number.toInt().inc()
            continueListSyntax(cursorBeforeEnter, paragraphMargin, syntax = "$nextNumber. ")
          }
        }
      }

      return null
    }

    private fun continueListSyntax(
      cursorBeforeEnter: TextSelection,
      paragraphMargin: CharSequence,
      syntax: String
    ): ReplaceNewLineWith {
      val syntaxWithLineBreak = "\n$paragraphMargin$syntax"
      return InsertLetters(
          replacement = syntaxWithLineBreak,
          newSelection = cursorBeforeEnter.offsetBy(syntaxWithLineBreak.length)
      )
    }

    private fun endListSyntax(lastItemBounds: ParagraphBounds): ReplaceNewLineWith {
      return DeleteLetters(deleteCount = lastItemBounds.endExclusive - lastItemBounds.start)
    }
  }
}
