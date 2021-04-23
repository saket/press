package me.saket.press.shared.ui

data class HighlightedText(
  val text: String,
  val highlight: IntRange? = null,
)

fun String.highlight(searchText: String): HighlightedText {
  val highlightStart = this.indexOf(searchText, ignoreCase = true)
  return if (highlightStart == -1) {
    HighlightedText(this)
  } else {
    HighlightedText(this, highlight = highlightStart..(highlightStart + searchText.length))
  }
}
