package me.saket.wysiwyg

import me.saket.wysiwyg.parser.MarkdownParser
import me.saket.wysiwyg.parser.RealtimeMarkdownRenderer
import me.saket.wysiwyg.parser.StaticMarkdownRenderer
import me.saket.wysiwyg.parser.highlighters.RootNodeHighlighter
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.widgets.AfterTextChange
import me.saket.wysiwyg.widgets.NativeTextField
import me.saket.wysiwyg.widgets.StyledText
import me.saket.wysiwyg.widgets.text

class Wysiwyg(
  private val textField: NativeTextField,
  style: WysiwygStyle
) {
  private val parser = MarkdownParser()
  private val renderer = RealtimeMarkdownRenderer(style, textField)
  private val bgExecutor = SingleThreadBackgroundExecutor()
  private val uiExecutor = UiThreadExecutor

  fun syntaxHighlighter() = AfterTextChange { text ->
    val immutableText = text.toString()
    val textLengthToParse = immutableText.length

    bgExecutor.enqueue {
      val rootNode = parser.parseSpans(immutableText)

      renderer.clear()
      RootNodeHighlighter.visit(rootNode, renderer)

      uiExecutor.enqueue {
        // Because the text is parsed in background, it is possible
        // that the text changes faster than they get processed.
        val isStale = textLengthToParse != textField.text.length

        if (isStale.not()) {
          suspendTextChangesAndRun {
            parser.removeSpans(text)
            renderer.renderTo(text)
          }
        }
      }
    }
  }

  companion object {
    fun highlightImmediately(markdown: String, style: WysiwygStyle): StyledText {
      val rootNode = MarkdownParser().parseSpans(markdown)
      return StaticMarkdownRenderer(style).render(rootNode, markdown)
    }
  }
}
