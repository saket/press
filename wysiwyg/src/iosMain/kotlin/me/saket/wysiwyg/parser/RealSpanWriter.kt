package me.saket.wysiwyg.parser

import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.widgets.EditableText
import me.saket.wysiwyg.widgets.NativeTextField

actual class RealSpanWriter actual constructor(textField: NativeTextField): SpanWriter {
  override fun add(span: WysiwygSpan, start: Int, end: Int): Unit = TODO()
  override fun writeTo(text: EditableText): Unit = TODO()
  override fun clear(): Unit = TODO()
}
