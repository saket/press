package me.saket.press.shared.theme

import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.style.WysiwygStyle.BlockQuote
import me.saket.wysiwyg.style.WysiwygStyle.Code
import me.saket.wysiwyg.style.WysiwygStyle.Heading
import me.saket.wysiwyg.style.WysiwygStyle.Link
import me.saket.wysiwyg.style.WysiwygStyle.ThematicBreak
import kotlin.math.roundToInt

abstract class ThemePalette(
  val primaryColor: Int,
  val primaryColorDark: Int,
  val accentColor: Int,
  val textHighlightColor: Int,
  val window: WindowPalette,
  val markdown: MarkdownPalette,
  val textColorHeading: Int,
  val textColorPrimary: Int,
  val textColorSecondary: Int,
  val fabColor: Int
)

data class WindowPalette(
  val backgroundColor: Int,
  val editorBackgroundColor: Int
)

data class MarkdownPalette(
  val heading: Int,
  val syntaxColor: Int,
  val blockQuoteVerticalRuleColor: Int,
  val blockQuoteTextColor: Int,
  val linkUrlColor: Int,
  val linkTextColor: Int,
  val thematicBreakColor: Int,
  val codeBackgroundColor: Int,
  val headingTextColor: Int
)

fun WysiwygStyle.Companion.from(palette: MarkdownPalette, displayUnits: DisplayUnits) =
  WysiwygStyle(
      syntaxColor = palette.syntaxColor,
      blockQuote = BlockQuote(
          leftBorderColor = palette.blockQuoteTextColor,
          leftBorderWidth = displayUnits.fromPixels(4).roundToInt(),
          indentationMargin = displayUnits.fromPixels(24).roundToInt(),
          textColor = palette.blockQuoteTextColor
      ),
      list = WysiwygStyle.List(
          indentationMargin = displayUnits.fromPixels(24).roundToInt()
      ),
      link = Link(
          titleTextColor = palette.linkTextColor,
          urlTextColor = palette.linkUrlColor
      ),
      thematicBreak = ThematicBreak(
          color = palette.thematicBreakColor,
          height = displayUnits.fromPixels(4)
      ),
      code = Code(
          backgroundColor = palette.codeBackgroundColor,
          codeBlockMargin = displayUnits.fromPixels(8).roundToInt()
      ),
      heading = Heading(
          textColor = palette.headingTextColor
      )
  )
