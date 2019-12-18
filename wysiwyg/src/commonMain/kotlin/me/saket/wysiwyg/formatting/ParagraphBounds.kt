package me.saket.wysiwyg.formatting

data class ParagraphBounds(val startIndex: Int, val endIndex: Int) {

  companion object {
    /**
     * Finds the beginning/ending of a paragraph under a cursor position.
     */
    fun find(text: String, cursorPosition: Int): ParagraphBounds {
      // Begin with the assumption that this is the first paragraph
      var start = 0
      for (i in cursorPosition downTo 0) {
        if (i > 0 && text[i - 1] == '\n') {
          start = i
          break
        }
      }

      // Begin with the assumption that this is the last paragraph.
      var end = text.length
      for (i in cursorPosition until text.length) {
        if (text[i] == '\n') {  // TODO: avoid including the newline char.
          end = i
          break
        }
      }

      return ParagraphBounds(start, end)
    }
  }
}
