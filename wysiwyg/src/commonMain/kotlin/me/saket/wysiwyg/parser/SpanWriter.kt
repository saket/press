package me.saket.wysiwyg.parser

import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.widgets.EditableText

expect class SpanWriter() {
  fun add(span: WysiwygSpan, start: Int, end: Int)
  fun writeTo(text: EditableText)
}
