package me.saket.press.shared.theme

import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.style.WysiwygStyle.BlockQuote
import me.saket.wysiwyg.style.WysiwygStyle.Code
import me.saket.wysiwyg.style.WysiwygStyle.Heading
import me.saket.wysiwyg.style.WysiwygStyle.Link
import me.saket.wysiwyg.style.WysiwygStyle.ThematicBreak
import kotlin.DeprecationLevel.ERROR
import kotlin.math.roundToInt

abstract class ThemePalette(
  val isLightTheme: Boolean,
  val primaryColor: Int,
  val primaryColorDark: Int,
  val accentColor: Int,
  val textHighlightColor: Int,
  val window: WindowPalette,
  val markdown: MarkdownPalette,
  val textColorHeading: Int,
  val textColorPrimary: Int,
  val textColorSecondary: Int,
  val textColorHint: Int,
  val fabColor: Int
) {

  private companion object {
    private const val BLACK = 0xFF000000.toInt()
    private const val WHITE = 0xFFFFFFFF.toInt()
  }

  val fabIcon: Int
    get() = fabColor.blendWith(if (isLightTheme) WHITE else BLACK, ratio = 0.65f)

  // todo: rename to divider
  val separator: Int
    get() = window.backgroundColor.blendWith(BLACK, ratio = 0.2f)

  val buttonNormal: Int
    get() = window.backgroundColor.blendWith(if (isLightTheme) WHITE else BLACK, ratio = 0.2f)

  val buttonPressed: Int
    get() = window.backgroundColor.blendWith(if (isLightTheme) WHITE else BLACK, ratio = 0.5f)
}

data class WindowPalette(
  val backgroundColor: Int,
  val editorBackgroundColor: Int  // todo: migrate to elevatedBackgroundColor
) {
  val elevatedBackgroundColor: Int get() = editorBackgroundColor
}

data class MarkdownPalette(
  val blockQuoteVerticalRuleColor: Int,
  val blockQuoteTextColor: Int,
  val codeBackgroundColor: Int,
  val headingTextColor: Int,
  val linkTextColor: Int,
  val linkUrlColor: Int,
  val strikethroughTextColor: Int,
  val syntaxColor: Int,
  val thematicBreakColor: Int
)

fun WysiwygStyle.Companion.from(palette: MarkdownPalette, displayUnits: DisplayUnits) =
  WysiwygStyle(
    syntaxColor = palette.syntaxColor,
    strikethroughTextColor = palette.strikethroughTextColor,
    blockQuote = BlockQuote(
      leftBorderColor = palette.blockQuoteTextColor,
      leftBorderWidth = displayUnits.scaledPixels(4).roundToInt(),
      indentationMargin = displayUnits.scaledPixels(24).roundToInt(),
      textColor = palette.blockQuoteTextColor
    ),
    code = Code(
      backgroundColor = palette.codeBackgroundColor,
      codeBlockMargin = displayUnits.scaledPixels(8).roundToInt()
    ),
    heading = Heading(
      textColor = palette.headingTextColor
    ),
    link = Link(
      textColor = palette.linkTextColor,
      urlColor = palette.linkUrlColor
    ),
    list = WysiwygStyle.List(
      indentationMargin = displayUnits.scaledPixels(8).roundToInt()
    ),
    thematicBreak = ThematicBreak(
      color = palette.thematicBreakColor,
      height = displayUnits.scaledPixels(4)
    )
  )

@Deprecated("use separator", level = ERROR, replaceWith = ReplaceWith("separator"))
val ThemePalette.divider: Int get() = separator
