package me.saket.wysiwyg.formatting

import kotlin.test.Test

abstract class BaseMarkdownSyntaxApplierTest {
  @Test abstract fun `insert at cursor position`()
  @Test abstract fun `apply to selection`()

  protected fun buildSelection(text: String): Pair<String, TextSelection> {
    val markerCount = text.count { it == '▮' }
    require(markerCount in 1..2) {
      when (markerCount) {
        0 -> "Text has no cursor markers"
        else -> "Text has >2 ($markerCount) selection markers"
      }
    }

    val selection = when (markerCount) {
      1 -> TextSelection.cursor(text.indexOfFirst { it == '▮' })
      else -> TextSelection(
          start = text.indexOfFirst { it == '▮' },
          end = text.indexOfLast { it == '▮' } - 1
      )
    }
    return text.replace("▮", "") to selection
  }
}
