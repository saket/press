package me.saket.wysiwyg.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import me.saket.wysiwyg.parser.highlighters.RootNodeHighlighter
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.style.parseColor

class MarkdownParserTest {

  private val style = wysiwygStyle()
  private val writer = FakeMarkdownRenderer(style)
  private val parser = MarkdownParser()

  /*@Test*/ fun bold() {
    parseAndCompare(
        input = """
                |It's not who you are underneath. 
                |It's **what** you do that defines you.
                """,
        expect = """
                |It's not who you are underneath. 
                |It's <bold>**what**</bold> you do that defines you.
                """
    )
  }

  /*@Test*/ fun italic() {
    parseAndCompare(
        input = """
                |Why do we fall? 
                |So we can *learn* to pick ourselves back up.
                """,
        expect = """
                |Why do we fall? 
                |So we can <italic>*learn*</italic> to pick ourselves back up.
                """
    )
  }

  /*@Test*/ fun strikethrough() {
    parseAndCompare(
        input = """
                |How can two people ~~love~~ hate so much 
                |without knowing each other?
                """,
        expect = """
                |How can two people <strike>~~love~~</strike> hate so much 
                |without knowing each other?
                """
    )
  }

  @Suppress("NAME_SHADOWING")
  private fun parseAndCompare(input: String, expect: String) {
    val input = input.trimMargin()
    val expect = expect.trimMargin()

    val node = parser.parseSpans(input)
    RootNodeHighlighter.visit(node, writer)
    val html = writer.renderHtml(input)

    if (html != expect) {
      println("--------------------------------------")
      println("Text doesn't match.")
      println("Expected:\n\"\"\"\n$expect\n\"\"\"")
      println("\nActual: \n\"\"\"\n$html\n\"\"\"")
    }
    assertThat(html).isEqualTo(expect)
  }

  companion object {
    private fun wysiwygStyle(): WysiwygStyle {
      return WysiwygStyle(
          syntaxColor = "#CCAEF9".parseColor(),
          strikethroughTextColor = "#9E9E9E".parseColor(),
          blockQuote = WysiwygStyle.BlockQuote(
              leftBorderColor = "#353846".parseColor(),
              leftBorderWidth = 4,
              indentationMargin = 8,
              textColor = "#CED2F8".parseColor()
          ),
          code = WysiwygStyle.Code(backgroundColor = "#242632".parseColor(), codeBlockMargin = 12),
          heading = WysiwygStyle.Heading(textColor = "#85F589".parseColor()),
          link = WysiwygStyle.Link(textColor = "#8DF0FF".parseColor(), urlColor = "#C6D1FF".parseColor()),
          list = WysiwygStyle.List(indentationMargin = 16),
          thematicBreak = WysiwygStyle.ThematicBreak(color = "#62677C".parseColor(), height = 20f)
      )
    }
  }
}
