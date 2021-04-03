package me.saket.wysiwyg.parser

import android.text.SpannableStringBuilder
import android.text.Spanned
import me.saket.wysiwyg.parser.highlighters.RootNodeHighlighter
import me.saket.wysiwyg.parser.node.Node
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.widgets.StyledText

actual class StaticMarkdownRenderer actual constructor(style: WysiwygStyle) : BaseMarkdownRenderer(style) {
  actual fun renderWith(markdownNode: Node, text: String): StyledText {
    RootNodeHighlighter.visit(markdownNode, this)

    return SpannableStringBuilder(text).apply {
      for ((span, start, end) in queuedSpans) {
        setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }
  }
}
