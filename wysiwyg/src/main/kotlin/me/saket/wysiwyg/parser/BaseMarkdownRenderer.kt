package me.saket.wysiwyg.parser

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

actual abstract class BaseMarkdownRenderer(style: WysiwygStyle) : MarkdownRenderer(style) {
  private val spanPool = SpanPool()
  protected val queuedSpans = mutableListOf<Triple<Any, Int, Int>>()

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
