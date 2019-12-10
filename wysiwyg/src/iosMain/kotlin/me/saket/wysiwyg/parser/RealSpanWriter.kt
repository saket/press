package me.saket.wysiwyg.parser

import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.widgets.EditableText
import me.saket.wysiwyg.widgets.NativeTextField

actual class RealSpanWriter actual constructor(style: WysiwygStyle, textField: NativeTextField): SpanWriter(style) {
  override fun writeTo(text: EditableText): Unit = TODO()
  override fun clear(): Unit = TODO()
}
