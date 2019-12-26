package me.saket.wysiwyg.formatting

data class ParagraphBounds(
  val start: Int,
  /**
   * For use with String#substring(). Points to the
   * line break character if any or the text length.
   */
  val endExclusive: Int
) {

  companion object {
    /**
     * Finds the beginning/ending of paragraph(s) under a cursor position or
     * null if `text` is empty. Unlike functions in [String], this does not
     * return -1 for empty paragraphs. See [ParagraphBoundsTest].
     */
    fun find(text: String, selection: TextSelection): ParagraphBounds {
      // Begin with the assumption that this is the first paragraph
      var start = 0
      for (i in selection.start downTo 0) {
        if (i > 0 && text[i - 1] == '\n') {
          start = i
          break
        }
      }

      // Begin with the assumption that this is the last paragraph.
      var endExclusive = text.length
      for (i in selection.end until text.length) {
        if (text[i] == '\n') {
          endExclusive = i
          break
        }
      }

      return ParagraphBounds(start, endExclusive)
    }
  }
}
