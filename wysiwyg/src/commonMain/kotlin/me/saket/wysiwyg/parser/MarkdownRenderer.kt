package me.saket.wysiwyg.parser

import me.saket.wysiwyg.parser.node.HeadingLevel
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.widgets.EditableText
import me.saket.wysiwyg.widgets.NativeTextField
import me.saket.wysiwyg.widgets.StyledText

/**
 * Collects spans on a background thread so that they can later be written to
 * the text field (using [MarkdownRenderer.renderTo]) in one go on the main thread.
 */
abstract class MarkdownRenderer(val style: WysiwygStyle) {
  abstract fun addForegroundColor(color: Int, from: Int, to: Int)
  abstract fun addItalics(from: Int, to: Int)
  abstract fun addBold(from: Int, to: Int)
  abstract fun addStrikethrough(from: Int, to: Int)
  abstract fun addInlineCode(from: Int, to: Int)
  abstract fun addMonospaceTypeface(from: Int, to: Int)
  abstract fun addIndentedCodeBlock(from: Int, to: Int)
  abstract fun addQuote(from: Int, to: Int)
  abstract fun addLeadingMargin(margin: Int, from: Int, to: Int)
  abstract fun addHeading(level: HeadingLevel, from: Int, to: Int)
  abstract fun addClickableUrl(url: String, from: Int, to: Int)
  abstract fun addThematicBreak(syntax: String, from: Int, to: Int)
}

expect abstract class BaseMarkdownRenderer : MarkdownRenderer

/** Renderer for an editable text field. */
expect class RealtimeMarkdownRenderer(style: WysiwygStyle, textField: NativeTextField) : BaseMarkdownRenderer {
  fun renderTo(text: EditableText)
  fun clear()
}

/** Renderer for an static text view. */
expect class StaticMarkdownRenderer(style: WysiwygStyle) : BaseMarkdownRenderer {
  fun render(markdownNode: Node, text: String): StyledText
}
