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

  private object StartFencedCodeBlock : OnEnterAutoFormatter {
    val fencedCodeRegex by lazy(NONE) { Regex("```[a-z]*[\\s\\S]*?```") }

    override fun onEnter(text: String, paragraph: String, selection: TextSelection): ApplyMarkdownSyntax? {
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
    private val orderedItemRegex by lazy(NONE) { Regex("(\\d+).\\s") }

    @Suppress("NAME_SHADOWING")
    override fun onEnter(text: String, paragraph: String, selection: TextSelection): ApplyMarkdownSyntax? {
      val buildForSyntax = { syntax: String ->
        val cursor = selection.cursorPosition
        val syntaxWithLineBreak = "\n$syntax"
        ApplyMarkdownSyntax(
            newText = text.substring(0, cursor) + syntaxWithLineBreak + text.substring(cursor),
            newSelection = selection.offsetBy(syntaxWithLineBreak.length)
        )
      }

      val paragraph = paragraph.trimStart()

      // Unordered list item.
      if (paragraph[0] in "*+-" && paragraph[1].isWhitespace()) {
        return buildForSyntax("${paragraph[0]} ")
      }

      // Ordered list item.
      val potentiallyOrderedItem = paragraph[0].isDigit()
      if (potentiallyOrderedItem) {
        val matchResult = orderedItemRegex.find(paragraph)
        if (matchResult != null) {
          val (number) = matchResult.destructured
          val nextNumber = number.toInt().inc()
          return buildForSyntax("$nextNumber. ")
        }
      }

      return null
    }
  }
}

