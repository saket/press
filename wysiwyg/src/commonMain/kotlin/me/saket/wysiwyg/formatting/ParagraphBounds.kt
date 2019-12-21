package me.saket.wysiwyg.formatting

data class ParagraphBounds(
  val start: Int,
  val end: Int,
  val endExclusive: Int // For use with String#substring().
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
      var end = if (text.isEmpty()) 0 else text.length - 1
      var endExclusive = text.length

      for (i in selection.end until text.length) {
        if (text[i] == '\n') {
          end = i - 1
          endExclusive = i
          break
        }
      }

      // An example where this is needed is text with a single
      // '\n' character and the cursor is at the end.
      end = maxOf(start, end)

      // TODO: remove this
      val trytry = text.substring(start, endExclusive)

      return ParagraphBounds(start, end, endExclusive)
    }
  }
}
