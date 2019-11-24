package me.saket.wysiwyg

import me.saket.wysiwyg.parser.MarkdownParser
import me.saket.wysiwyg.parser.SpanWriter
import me.saket.wysiwyg.parser.highlighters.RootNodeHighlighter
import me.saket.wysiwyg.spans.SpanPool
import me.saket.wysiwyg.theme.WysiwygTheme
import me.saket.wysiwyg.widgets.AfterTextChange
import me.saket.wysiwyg.widgets.NativeTextField
import me.saket.wysiwyg.widgets.text

class Wysiwyg(
  private val textField: NativeTextField,
  theme: WysiwygTheme
) {

  private val parser = MarkdownParser()
  private val spanPool = SpanPool(theme)

  private val bgExecutor = SingleThreadBackgroundExecutor()
  private val uiExecutor = UiThreadExecutor
  private val spanWriter = SpanWriter(textField)

  fun syntaxHighlighter() = AfterTextChange { text ->
    val immutableText = text.toString()
    val textLengthToParse = immutableText.length

    bgExecutor.enqueue {
      val rootNode = parser.parseSpans(immutableText)

      spanWriter.clear()
      RootNodeHighlighter.visit(rootNode, spanPool, spanWriter)

      uiExecutor.enqueue {
        // Because the text is parsed in background, it is possible
        // that the text changes faster than they get processed.
        val isStale = textLengthToParse != textField.text.length

        if (isStale.not()) {
          suspendTextChangesAndRun {
            parser.removeSpans(text)
            spanWriter.writeTo(text)
          }
        }
      }
    }
  }
}
