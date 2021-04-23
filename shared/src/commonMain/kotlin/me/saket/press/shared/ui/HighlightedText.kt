package me.saket.press.shared.ui

data class HighlightedText(
  val text: String,
  val highlight: IntRange? = null,
)

fun String.highlight(searchText: String): HighlightedText {
  if (searchText.isBlank()) {
    return HighlightedText(this)
  }
  return when (val start = this.indexOf(searchText, ignoreCase = true)) {
    -1 -> HighlightedText(this)
    else -> HighlightedText(this, highlight = start..(start + searchText.length))
  }
}

private val whitespaceRegex = Regex("\\s+")

fun String.highlightInNoteBody(searchText: String): HighlightedText {
  // Trim multiple whitespaces (space, new line, et al) so that more characters fit in the note list.
  val body = this.replace(whitespaceRegex, " ")

  if (searchText.isBlank()) {
    return HighlightedText(body)
  }
  val highlightStart = body.indexOf(searchText, ignoreCase = true)
  if (highlightStart == -1) {
    return HighlightedText(body)
  }

  // Because Press only shows a note's body upto (currently) 2 lines, the highlighted text may be
  // located beyond the limit and not show up in search results despite being present in the body.
  // When this is true, the search term is brought into the visible window by truncating words on
  // the left. This was copied from https://bear.app.
  var truncateFrom = 0
  var spacesCounted = 0

  for (i in (highlightStart - 1) downTo 0) {
    spacesCounted += if (body[i] == ' ') 1 else 0
    if (spacesCounted == 2) {
      break
    }
    truncateFrom = i
  }

  val truncatedText = if (truncateFrom > 0) "... ${body.substring(truncateFrom)}" else body
  val truncatedHighlightStart = truncatedText.indexOf(searchText, ignoreCase = true)

  return HighlightedText(
    text = truncatedText,
    highlight = truncatedHighlightStart..(truncatedHighlightStart + searchText.length)
  )
}
