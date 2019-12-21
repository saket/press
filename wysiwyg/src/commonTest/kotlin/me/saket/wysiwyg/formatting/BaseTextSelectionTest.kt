package me.saket.wysiwyg.formatting

abstract class BaseTextSelectionTest {

  protected fun decodeSelection(text: String): Pair<String, TextSelection> {
    val markerCount = text.count { it == '▮' }
    require(markerCount in 1..2) {
      when (markerCount) {
        0 -> "Text has no cursor markers"
        else -> "Text has >2 ($markerCount) selection markers"
      }
    }

    val selection = when (markerCount) {
      1 -> TextSelection.cursor(
          text.indexOfFirst { it == '▮' })
      else -> TextSelection(
          start = text.indexOfFirst { it == '▮' },
          end = text.indexOfLast { it == '▮' } - 1
      )
    }
    return text.replace("▮", "") to selection
  }

  protected fun encodeSelection(text: String, selection: TextSelection): String {
    return if (selection.isCursor) {
      text.substring(0, selection.cursorPosition) + "▮" + text.substring(selection.cursorPosition, text.length)
    } else {
      text.substring(0, selection.start) +
          "▮" + text.substring(selection.start, selection.end) + "▮" +
          text.substring(selection.end, text.length)
    }
  }
}
