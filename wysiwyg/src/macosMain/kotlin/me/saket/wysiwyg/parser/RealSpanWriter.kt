package me.saket.wysiwyg.parser

import me.saket.wysiwyg.parser.node.HeadingLevel
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.widgets.EditableText
import me.saket.wysiwyg.widgets.NativeTextField

actual class RealSpanWriter actual constructor(style: WysiwygStyle, textField: NativeTextField): SpanWriter(style) {
  override fun writeTo(text: EditableText): Unit = TODO()
  override fun clear(): Unit = TODO()
  override fun addForegroundColor(color: Int, from: Int, to: Int): Unit = TODO()
  override fun addItalics(from: Int, to: Int): Unit = TODO()
  override fun addBold(from: Int, to: Int): Unit = TODO()
  override fun addStrikethrough(from: Int, to: Int): Unit = TODO()
  override fun addInlineCode(from: Int, to: Int): Unit = TODO()
  override fun addMonospaceTypeface(from: Int, to: Int): Unit = TODO()
  override fun addIndentedCodeBlock(from: Int, to: Int): Unit = TODO()
  override fun addQuote(from: Int, to: Int): Unit = TODO()
  override fun addLeadingMargin(margin: Int, from: Int, to: Int): Unit = TODO()
  override fun addHeading(level: HeadingLevel, from: Int, to: Int): Unit = TODO()
  override fun addClickableUrl(url: String, from: Int, to: Int): Unit = TODO()
  override fun addThematicBreak(syntax: String, from: Int, to: Int): Unit = TODO()
}
