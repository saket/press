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

  fun onEnter(text: String, selection: TextSelection): ApplyMarkdownSyntax? {
    if (selection.isCursor.not()) {
      return null
    }

    // Paragraph under cursor.
    val paragraphBounds = ParagraphBounds.find(text, selection)
    val paragraph = text.substring(paragraphBounds.start, paragraphBounds.endExclusive)

    if (paragraph.isBlank()) {
      return null
    }

    return formatters
        .mapNotNull { it.onEnter(text, paragraph, paragraphBounds, selection) }
        .firstOrNull()
  }

  private interface OnEnterAutoFormatter {
    /**
     * @param paragraph Paragraph under the cursor before enter key was pressed.
     * @param selection cursor position before enter key was pressed.
     */
    fun onEnter(
      text: String,
      paragraph: String,
      paragraphBounds: ParagraphBounds,
      selection: TextSelection
    ): ApplyMarkdownSyntax?
  }

  private object StartFencedCodeBlock : OnEnterAutoFormatter {
    val fencedCodeRegex by lazy(NONE) { Regex("```[a-z]*[\\s\\S]*?```") }

    override fun onEnter(
      text: String,
      paragraph: String,
      paragraphBounds: ParagraphBounds,
      selection: TextSelection
    ): ApplyMarkdownSyntax? {
      if (!paragraph.startsWith("```")) {
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

  private object ListContinuation : OnEnterAutoFormatter {
    // TODO: Can this be optimized by removing regex?
    private val orderedItemRegex by lazy(NONE) { Regex("(\\d+).\\s") }

    @Suppress("NAME_SHADOWING")
    override fun onEnter(
      text: String,
      paragraph: String,
      paragraphBounds: ParagraphBounds,
      selection: TextSelection
    ): ApplyMarkdownSyntax? {
      val paragraph = paragraph.trimStart()

      // Unordered list item.
      if (paragraph.length >= 2 && paragraph[0] in "*+-" && paragraph[1].isWhitespace()) {
        val isItemEmpty = paragraph.length == 2
        return if (isItemEmpty) {
          endListSyntax(text, paragraphBounds)
        } else {
          continueListSyntax(text, selection, syntax = "${paragraph[0]} ")
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
            continueListSyntax(text, selection, syntax = "$nextNumber. ")
          }
        }
      }

      return null
    }

    private fun continueListSyntax(text: String, selection: TextSelection, syntax: String): ApplyMarkdownSyntax {
      val cursor = selection.cursorPosition
      val syntaxWithLineBreak = "\n$syntax"
      return ApplyMarkdownSyntax(
          newText = text.substring(0, cursor) + syntaxWithLineBreak + text.substring(cursor),
          newSelection = selection.offsetBy(syntaxWithLineBreak.length)
      )
    }

    private fun endListSyntax(text: String, paragraphBounds: ParagraphBounds): ApplyMarkdownSyntax {
      return ApplyMarkdownSyntax(
          newText = text.substring(0, paragraphBounds.start) + "\n" + text.substring(paragraphBounds.endExclusive),
          newSelection = TextSelection.cursor(paragraphBounds.start + 1)
      )
    }
  }
}

