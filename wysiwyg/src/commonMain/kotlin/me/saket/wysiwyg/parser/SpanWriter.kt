package me.saket.wysiwyg.parser

import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.widgets.EditableText
import me.saket.wysiwyg.widgets.NativeTextField

typealias SpanStart = Int
typealias SpanEnd = Int
typealias LineNumber = Int

/**
 * Collects spans on a background thread so that they
 * can later be written in one go on the main thread.
 */
expect class SpanWriter(textField: NativeTextField) {
  fun add(span: WysiwygSpan, start: Int, end: Int)
  fun writeTo(text: EditableText)
  fun clear()
}
