package me.saket.wysiwyg.formatting

import kotlin.test.Test

abstract class BaseMarkdownSyntaxApplierTest {
  @Test abstract fun `insert at cursor position`()
  @Test abstract fun `apply to selection`()

  protected fun buildSelection(text: String): Pair<String, TextSelection> {
    val markers = text.count { it == '▮' }
    require(markers in 1..2)

    val selection = when (markers) {
      1 -> TextSelection.cursor(text.indexOfFirst { it == '▮' })
      else -> TextSelection(
          start = text.indexOfFirst { it == '▮' },
          end = text.indexOfLast { it == '▮' } - 1
      )
    }
    return text.replace("▮", "") to selection
  }
}
