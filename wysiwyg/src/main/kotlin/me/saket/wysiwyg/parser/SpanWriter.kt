package me.saket.wysiwyg.parser

import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import me.saket.wysiwyg.parser.node.HeadingLevel
import me.saket.wysiwyg.spans.HeadingSpan
import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.widgets.EditableText
import me.saket.wysiwyg.widgets.NativeTextField

actual class SpanWriter actual constructor(private val textField: NativeTextField) {

  private val spans = mutableListOf<Triple<Any, SpanStart, SpanEnd>>()

  private val newHeadings = mutableSetOf<Pair<LineNumber, HeadingLevel>>()
  private val lastHeadings = mutableSetOf<Pair<LineNumber, HeadingLevel>>()

  actual fun add(span: WysiwygSpan, start: SpanStart, end: SpanEnd) {
    spans.add(Triple(span, start, end))
  }

  actual fun writeTo(text: EditableText) {
    for ((span, start) in spans) {
      if (span is HeadingSpan) {
        newHeadings.add(textField.layout.getLineForOffset(start) to span.level)
      }
    }

    val headingSpansUpdated = newHeadings != lastHeadings
    newHeadings.clear()
    lastHeadings.clear()

    for ((span, start, end) in spans) {
      text.setSpan(span, start, end, SPAN_EXCLUSIVE_EXCLUSIVE)

      if (span is HeadingSpan) {
        lastHeadings.add(textField.layout.getLineForOffset(start) to span.level)
      }
    }
    clear()

    // TextView's layout doesn't always recalculate line heights when a
    // LineHeightSpan is added or updated. Resetting the text is expensive,
    // so it's done only when needed.
    if (headingSpansUpdated) {
      val start = textField.selectionStart
      val end = textField.selectionEnd
      textField.text = text
      textField.setSelection(start, end)
    }
  }

  actual fun clear() {
    spans.clear()
  }
}
