package me.saket.wysiwyg.parser

import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import me.saket.wysiwyg.parser.node.HeadingLevel
import me.saket.wysiwyg.spans.HeadingSpan
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.widgets.EditableText
import me.saket.wysiwyg.widgets.NativeTextField

private typealias LineNumber = Int

actual class RealtimeMarkdownRenderer actual constructor(
  style: WysiwygStyle,
  private val textField: NativeTextField
) : BaseMarkdownRenderer(style) {

  private val newHeadings = mutableSetOf<Pair<LineNumber, HeadingLevel>>()
  private val lastHeadings = mutableSetOf<Pair<LineNumber, HeadingLevel>>()

  actual fun renderTo(text: EditableText) {
    for ((span, start) in queuedSpans) {
      if (textField.layout != null && span is HeadingSpan) {
        newHeadings.add(textField.layout.getLineForOffset(start) to span.level)
      }
    }

    val headingSpansUpdated = newHeadings != lastHeadings
    newHeadings.clear()
    lastHeadings.clear()

    for ((span, start, end) in queuedSpans) {
      text.setSpan(span, start, end, SPAN_EXCLUSIVE_EXCLUSIVE)

      if (textField.layout != null && span is HeadingSpan) {
        lastHeadings.add(textField.layout.getLineForOffset(start) to span.level)
      }
    }
    clear()

    // TextView's layout doesn't always recalculate line heights when a
    // LineHeightSpan is added or updated. Recreating the text layout is
    // expensive, so it's done only when needed.
    if (headingSpansUpdated) {
      // TextView#setHint() internally leads to checkForRelayout(). This
      // may stop working in the future, but TextView#setText() resets
      // the keyboard mode. Imagine pressing '#' on the symbols screen
      // and the keyboard resetting back to the alphabets screen.
      // Terrible experience if you're writing an H6.
      textField.hint = textField.hint
    }
  }

  actual fun clear() {
    queuedSpans.clear()
  }
}
