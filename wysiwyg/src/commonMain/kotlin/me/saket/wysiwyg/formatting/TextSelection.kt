package me.saket.wysiwyg.formatting

/**
 * Position of the cursor or the selection if any text is selected.
 * See [cursor].
 */
data class TextSelection(val start: Int, val end: Int) {
  val isCursor get() = start == end
  val cursorPosition get() = if (isCursor) start else throw AssertionError()

  companion object {
    /**
     * For use when the user isn't selecting any
     * text and the cursor is at one single location.
     */
    fun cursor(position: Int): TextSelection {
      return TextSelection(start = position, end = position)
    }
  }
}
