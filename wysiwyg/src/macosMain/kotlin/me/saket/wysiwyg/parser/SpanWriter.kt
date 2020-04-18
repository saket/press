package me.saket.wysiwyg.parser

import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.widgets.EditableText
import me.saket.wysiwyg.widgets.NativeTextField

actual class SpanWriter actual constructor(textField: NativeTextField) {
  actual fun add(span: WysiwygSpan, start: Int, end: Int): Unit = TODO()
  actual fun writeTo(text: EditableText): Unit = TODO()
  actual fun clear(): Unit = TODO()
}
