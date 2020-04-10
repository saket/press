package me.saket.wysiwyg

import android.content.Context
import android.graphics.Color
import android.graphics.Color.WHITE
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import app.cash.paparazzi.DeviceConfig.Companion.PIXEL_3
import app.cash.paparazzi.Paparazzi
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.style.WysiwygStyle.BlockQuote
import me.saket.wysiwyg.style.WysiwygStyle.Code
import me.saket.wysiwyg.style.WysiwygStyle.Heading
import me.saket.wysiwyg.style.WysiwygStyle.Link
import me.saket.wysiwyg.style.WysiwygStyle.ThematicBreak
import me.saket.wysiwyg.widgets.addTextChangedListener
import org.junit.Rule
import org.junit.Test
import kotlin.math.roundToInt

@Suppress("IllegalIdentifier")
class WysiwygTest {

  @get:Rule
  var paparazzi = Paparazzi(deviceConfig = PIXEL_3)

  private val context: Context = paparazzi.context

  private val Int.dp: Int
    get() {
      val metrics = context.resources.displayMetrics
      return TypedValue.applyDimension(COMPLEX_UNIT_DIP, this.toFloat(), metrics).roundToInt()
    }

  @Test fun `generate snapshot for rendering markdown`() {
    val editText = EditText(context).apply {
      layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
      setBackgroundColor(Color.DKGRAY)
      setTextColor(WHITE)
      setText(MARKDOWN)
    }

    val wysiwyg = Wysiwyg(
        textField = editText,
        style = wysiwygStyle(),
        bgExecutor = ImmediateExecutor(),
        uiExecutor = ImmediateExecutor()
    )
    editText.addTextChangedListener(wysiwyg.syntaxHighlighter())

    paparazzi.snapshot(editText, "Markdown rendering")
  }

  private fun wysiwygStyle(): WysiwygStyle {
    return WysiwygStyle(
        syntaxColor = 0xFFCCAEF9.toInt(),
        strikethroughTextColor = 0xFF9E9E9E.toInt(),
        blockQuote = BlockQuote(
            leftBorderColor = 0xFFCCAEF9.toInt(),
            leftBorderWidth = 4.dp,
            indentationMargin = 24.dp,
            textColor = 0xFFCED2F8.toInt()
        ),
        code = Code(
            backgroundColor = 0x5C121321,
            codeBlockMargin = 8.dp
        ),
        heading = Heading(
            textColor = 0xFF85F589.toInt()
        ),
        link = Link(
            textColor = 0xFF8DF0FF.toInt(),
            urlColor = 0xAAC6D1FF.toInt()
        ),
        list = WysiwygStyle.List(
            indentationMargin = 24.dp
        ),
        thematicBreak = ThematicBreak(
            color = 0xFF62677C.toInt(),
            height = 4.dp.toFloat()
        )
    )
  }

  companion object {
    const val MARKDOWN = """
      |# Markdown guide
      |Press understands standard markdown syntaxes, including: **bold**, *italic*, ~~strikethrough~~, and many more:
      |
      |### Code blocks
      |```
      |fun helloWorld() {
      |  println(""${'"'}
      |    Code blocks are wrapped inside 
      |    three ticks.
      |  ""${'"'})
      |}
      |``` 
      |
      |### Headings
      |Headings start with 1-6 `#` characters at the start of the line. 
      |
      |```
      |# Heading 1
      |## Heading 2
      |### Heading 3
      |#### Heading 4
      |##### Heading 5
      |###### Heading 6
      |```
      |
      |### Links
      |Links use a set of square brackets (`[]`) for describing the link text, followed by regular parentheses (`()`) containing the URL. 
      |
      |[Rick and Morty](https://www.imdb.com/title/tt2861424/).
      |
      |### Lists
      |Press supports ordered (numbered) and unordered (bulleted) lists. Unordered lists use asterisks, pluses, and hyphens — interchangeably — as list markers:
      |
      |- National Treasure
      |+ Ghost Rider
      |* Face/Off
      |
      |Ordered lists use numbers followed by periods:
      |
      |1. The Last of Us
      |2. Death Stranding
      |3. Cyberpunk 2077
      |
      |### Thematic breaks
      |Lines starting with three asterisks (`*`), hyphens (`-`) or underscores (`_`) are rendered as horizontal rules, a.k.a. “thematic breaks”. 
      |
      |---
      |
      |### Quotes
      |> A paragraph starting with a `>` are rendered as a quote. 
      """
  }
}
