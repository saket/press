package me.saket.wysiwyg.parser

import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.widgets.EditableText

actual class SpanWriter {
  actual fun add(span: WysiwygSpan, start: Int, end: Int): Unit = TODO()
  actual fun writeTo(text: EditableText): Unit = TODO()
}