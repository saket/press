package me.saket.wysiwyg.parser

import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.widgets.EditableText
import me.saket.wysiwyg.widgets.NativeTextField

typealias SpanStart = Int
typealias SpanEnd = Int

/**
 * Collects spans on a background thread so that they
 * can later be written in one go on the main thread.
 */
interface SpanWriter {
  fun add(span: WysiwygSpan, start: SpanStart, end: SpanEnd)
  fun writeTo(text: EditableText)
  fun clear()
}

expect class RealSpanWriter(textField: NativeTextField): SpanWriter
