package me.saket.wysiwyg.parser

import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import me.saket.wysiwyg.parser.node.HeadingLevel
import me.saket.wysiwyg.spans.BlockQuoteSpan
import me.saket.wysiwyg.spans.ClickableUrlSpan
import me.saket.wysiwyg.spans.ForegroundColorSpan
import me.saket.wysiwyg.spans.HeadingSpan
import me.saket.wysiwyg.spans.IndentedCodeBlockSpan
import me.saket.wysiwyg.spans.InlineCodeSpan
import me.saket.wysiwyg.spans.MonospaceTypefaceSpan
import me.saket.wysiwyg.spans.ParagraphLeadingMarginSpan
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.spans.StrikethroughSpan
import me.saket.wysiwyg.spans.StyleSpan
import me.saket.wysiwyg.spans.ThematicBreakSpan
import me.saket.wysiwyg.spans.WysiwygSpan
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.widgets.EditableText
import me.saket.wysiwyg.widgets.NativeTextField

typealias LineNumber = Int

actual class RealMarkdownRenderer actual constructor(
  style: WysiwygStyle,
  private val textField: NativeTextField
) : MarkdownRenderer(style) {

  private val spanPool = SpanPool()
  private val queuedSpans = mutableListOf<Triple<Any, Int, Int>>()
  private val newHeadings = mutableSetOf<Pair<LineNumber, HeadingLevel>>()
  private val lastHeadings = mutableSetOf<Pair<LineNumber, HeadingLevel>>()

  override fun renderTo(text: EditableText) {
    for ((span, start) in queuedSpans) {
      if (textField.layout != null && span is HeadingSpan) {
        newHeadings.add(textField.layout.getLineForOffset(start) to span.level)
      }
    }

    val headingSpansUpdated = newHeadings != lastHeadings
    newHeadings.clear()
    lastHeadings.clear()

    for ((span, start, end) in queuedSpans) {
      text.setSpan(span, start, end, SPAN_EXCLUSIVE_EXCLUSIVE)

      if (textField.layout != null && span is HeadingSpan) {
        lastHeadings.add(textField.layout.getLineForOffset(start) to span.level)
      }
    }
    clear()

    // TextView's layout doesn't always recalculate line heights when a
    // LineHeightSpan is added or updated. Recreating the text layout is
    // expensive, so it's done only when needed.
    if (headingSpansUpdated) {
      // TextView#setHint() internally leads to checkForRelayout(). This
      // may stop working in the future, but TextView#setText() resets
      // the keyboard. Imagine pressing '#' on the symbols screen and the
      // keyboard resetting back to the alphabets screen. Terrible
      // experience if you're writing an H6.
      textField.hint = textField.hint
    }
  }

  override fun clear() {
    queuedSpans.clear()
  }

  private fun enqueueSpan(span: WysiwygSpan, from: Int, to: Int) {
    queuedSpans.add(Triple(span, from, to))
  }

  override fun addForegroundColor(color: Int, from: Int, to: Int) {
    val foreground = spanPool.get(::ForegroundColorSpan).apply {
      this.color = color
    }
    enqueueSpan(foreground, from, to)
  }

  override fun addItalics(from: Int, to: Int) {
    val italics = spanPool.get(::StyleSpan).apply {
      style = StyleSpan.Style.ITALIC
    }
    enqueueSpan(italics, from, to)
  }

  override fun addBold(from: Int, to: Int) {
    val bold = spanPool.get(::StyleSpan).apply {
      style = StyleSpan.Style.BOLD
    }
    enqueueSpan(bold, from, to)
  }

  override fun addStrikethrough(from: Int, to: Int) {
    val strikethrough = spanPool.get(::StrikethroughSpan)
    enqueueSpan(strikethrough, from, to)
  }

  override fun addInlineCode(from: Int, to: Int) {
    val inlineCode = spanPool.get { InlineCodeSpan(style, it) }
    enqueueSpan(inlineCode, from, to)
  }

  override fun addMonospaceTypeface(from: Int, to: Int) {
    val monospaceTypeface = spanPool.get(::MonospaceTypefaceSpan)
    enqueueSpan(monospaceTypeface, from, to)
  }

  override fun addIndentedCodeBlock(from: Int, to: Int) {
    val indentedCodeBlock = spanPool.get { IndentedCodeBlockSpan(style, it) }
    enqueueSpan(indentedCodeBlock, from, to)
  }

  override fun addQuote(from: Int, to: Int) {
    val quote = spanPool.get { BlockQuoteSpan(style, it) }
    enqueueSpan(quote, from, to)
  }

  override fun addLeadingMargin(margin: Int, from: Int, to: Int) {
    val leadingMargin = spanPool.get { ParagraphLeadingMarginSpan(it) }.apply {
      this.margin = margin
    }
    enqueueSpan(leadingMargin, from, to)
  }

  override fun addHeading(level: HeadingLevel, from: Int, to: Int) {
    val heading = spanPool.get { HeadingSpan(it) }.apply {
      this.level = level
    }
    enqueueSpan(heading, from, to)
  }

  override fun addClickableUrl(url: String, from: Int, to: Int) {
    val clickableUrl = spanPool.get { ClickableUrlSpan(it) }.apply {
      this.url = url
    }
    enqueueSpan(clickableUrl, from, to)
  }

  override fun addThematicBreak(syntax: String, from: Int, to: Int) {
    val thematicBreak = spanPool.get { ThematicBreakSpan(style, it) }.apply {
      this.syntax = syntax
    }
    enqueueSpan(thematicBreak, from, to)
  }
}
