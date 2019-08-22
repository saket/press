package me.saket.wysiwyg.parser

import android.text.Spannable
import android.text.Spanned
import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.widgets.EditableText

/**
 * Inserts spans to [Spannable].
 */
actual class SpanWriter {

  private val spans = mutableListOf<Triple<Any, Int, Int>>()

  actual fun add(span: WysiwygSpan, start: Int, end: Int) {
    spans.add(Triple(span, start, end))
  }

  actual fun writeTo(text: EditableText) {
    for ((span, start, end) in spans) {
      text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    spans.clear()
  }
}