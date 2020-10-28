package me.saket.wysiwyg.parser

import me.saket.wysiwyg.parser.node.HeadingLevel
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.widgets.EditableText

private typealias Marker = String
private data class Position(val position: Int, val priority: Int)

@Suppress("NAME_SHADOWING")
class FakeMarkdownRenderer(style: WysiwygStyle) : MarkdownRenderer(style) {

  private val markers = mutableListOf<Pair<Position, Marker>>()
  private var minPriority = Int.MIN_VALUE
  private var maxPriority = Int.MAX_VALUE

  fun renderHtml(markdown: String): String {
    // SpanWriter doesn't get notified when plain text is read so it's
    // difficult to create a stack based marker system. Instead, markers
    // are sorted by their priorities and positions.
    val markers = markers
      .sortedByDescending { (pos, _) -> pos.priority }
      .sortedByDescending { (pos, _) -> pos.position }
      .map { (pos, marker) -> pos.position to marker }

    var html = markdown
    for ((position, marker) in markers) {
      html = html.substring(0..position) + marker + html.substring(position..html.length)
    }
    return html
  }

  private fun addMarker(marker: String, from: Int, to: Int) {
    markers += Position(from, minPriority++) to "<$marker>"
    markers += Position(to, maxPriority--) to "</$marker>"
  }

  override fun addForegroundColor(color: Int, from: Int, to: Int) {
    // Colors make the test results hard to read.
  }

  override fun addItalics(from: Int, to: Int) {
    addMarker("italic", from, to)
  }

  override fun addBold(from: Int, to: Int) {
    addMarker("bold", from, to)
  }

  override fun addStrikethrough(from: Int, to: Int) {
    addMarker("strike", from, to)
  }

  override fun addInlineCode(from: Int, to: Int) {
    TODO()
  }

  override fun addMonospaceTypeface(from: Int, to: Int) {
    TODO()
  }

  override fun addIndentedCodeBlock(from: Int, to: Int) {
    TODO()
  }

  override fun addQuote(from: Int, to: Int) {
    TODO()
  }

  override fun addLeadingMargin(margin: Int, from: Int, to: Int) {
    TODO()
  }

  override fun addHeading(level: HeadingLevel, from: Int, to: Int) {
    TODO()
  }

  override fun addClickableUrl(url: String, from: Int, to: Int) {
    TODO()
  }

  override fun addThematicBreak(syntax: String, from: Int, to: Int) {
    TODO()
  }

  override fun renderTo(text: EditableText) {
    error("Use renderHtml() instead.")
  }

  override fun clear() = Unit
}
