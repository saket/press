package me.saket.wysiwyg.parser

import me.saket.wysiwyg.parser.node.HeadingLevel
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.widgets.EditableText
import me.saket.wysiwyg.widgets.NativeTextField
import me.saket.wysiwyg.widgets.StyledText

actual class RealtimeMarkdownRenderer actual constructor(
  style: WysiwygStyle,
  textField: NativeTextField
) : BaseMarkdownRenderer(style) {
  actual fun renderTo(text: EditableText): Unit = TODO()
  actual fun clear(): Unit = TODO()
}

actual class StaticMarkdownRenderer actual constructor(style: WysiwygStyle) : BaseMarkdownRenderer(style) {
  actual fun renderWith(markdownNode: Node, text: String): StyledText = TODO()
}

actual abstract class BaseMarkdownRenderer(style: WysiwygStyle) : MarkdownRenderer(style) {
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
