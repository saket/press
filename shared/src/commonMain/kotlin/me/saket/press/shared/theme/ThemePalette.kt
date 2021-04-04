package me.saket.press.shared.theme

import com.github.ajalt.colormath.RGB
import com.github.ajalt.colormath.toCssRgb
import me.saket.wysiwyg.style.WysiwygStyle
import me.saket.wysiwyg.style.WysiwygStyle.BlockQuote
import me.saket.wysiwyg.style.WysiwygStyle.Code
import me.saket.wysiwyg.style.WysiwygStyle.Heading
import me.saket.wysiwyg.style.WysiwygStyle.Link
import me.saket.wysiwyg.style.WysiwygStyle.ThematicBreak
import me.saket.wysiwyg.style.withOpacity
import kotlin.DeprecationLevel.ERROR
import kotlin.math.roundToInt

abstract class ThemePalette(
  val name: String,
  val isLightTheme: Boolean,
  val primaryColor: Int,
  val primaryColorDark: Int,
  val accentColor: Int,
  val window: WindowPalette,
  val markdown: MarkdownPalette,
  val textColorHeading: Int,
  val textColorPrimary: Int,
  val textColorWarning: Int,
  val fabColor: Int
) {

  val fabIcon: Int
    get() = fabColor.blendWith(if (isLightTheme) WHITE else BLACK, ratio = 0.65f)

  // todo: rename to divider
  val separator: Int
    get() = BLACK.withOpacity(0.2f)

  val buttonNormal: Int
    get() = window.backgroundColor.blendWith(if (isLightTheme) WHITE else BLACK, ratio = 0.2f)

  // todo: inline
  val buttonPressed: Int
    get() = pressedColor(window.backgroundColor)

  val textColorSecondary =
    textColorPrimary.blendWith(window.backgroundColor, ratio = 0.30f)

  val textColorHint =
    textColorPrimary.blendWith(window.backgroundColor, ratio = 0.50f)

  // todo: rename to textColorHighlight
  val textHighlightColor =
    window.backgroundColor.blendWith(accentColor, ratio = 1f).withAlpha(0.4f)

  init {
    // Need these colors to be translucent to avoid covering the
    // cursor because it is drawn behind ForegroundColorSpan on Android.
    check(RGB.fromInt(textHighlightColor).alpha < 1f)
  }

  fun pressedColor(normalColor: Int): Int {
    val ratio = if (normalColor == TRANSPARENT) 0.2f else 0.5f
    return normalColor.blendWith(if (isLightTheme) WHITE else BLACK, ratio)
  }

  companion object {
    private const val BLACK = 0xFF000000.toInt()
    private const val WHITE = 0xFFFFFFFF.toInt()
    private const val TRANSPARENT: Int = 0

    fun lightThemePalettes(): List<ThemePalette> {
      return listOf(DraculaThemePalette, DraculaThemePalette, DraculaThemePalette, DraculaThemePalette)
    }

    fun darkThemePalettes(): List<ThemePalette> {
      return listOf(DraculaThemePalette, DraculaThemePalette, DraculaThemePalette, DraculaThemePalette)
    }
  }
}

data class WindowPalette(
  val backgroundColor: Int, // Equivalent to base3 color in terminal.
) {
  val elevatedBackgroundColor: Int = backgroundColor.toHsvColor()
    .lightenBy(-0.075f)
    .saturateBy(0.083f)
    .toRgbColorInt()
}

data class MarkdownPalette(
  val blockQuoteTextColor: Int,
  val linkTextColor: Int,
  val linkUrlColor: Int,
  val thematicBreakColor: Int
)

fun ThemePalette.wysiwygStyle(displayUnits: DisplayUnits): WysiwygStyle {
  return WysiwygStyle(
    syntaxColor = accentColor,
    strikethroughTextColor = textColorPrimary.blendWith(window.backgroundColor, .5f),
    blockQuote = BlockQuote(
      leftBorderColor = markdown.blockQuoteTextColor,
      leftBorderWidth = displayUnits.scaledPixels(4).roundToInt(),
      indentationMargin = displayUnits.scaledPixels(24).roundToInt(),
      textColor = markdown.blockQuoteTextColor
    ),
    code = Code(
      backgroundColor = window.backgroundColor.toHsvColor()
        .darkenBy(0.52f)
        .saturateBy(0.875f)
        .increaseHueBy(0.03f)
        .copy(a = 0.36f)
        .toRgbColorInt(),
      codeBlockMargin = displayUnits.scaledPixels(8).roundToInt()
    ),
    heading = Heading(
      textColor = textColorHeading
    ),
    link = Link(
      textColor = markdown.linkTextColor,
      urlColor = markdown.linkUrlColor
    ),
    list = WysiwygStyle.List(
      indentationMargin = displayUnits.scaledPixels(8).roundToInt()
    ),
    thematicBreak = ThematicBreak(
      color = markdown.thematicBreakColor,
      height = displayUnits.scaledPixels(4)
    )
  )
}

@Deprecated("use separator", level = ERROR, replaceWith = ReplaceWith("separator"))
val ThemePalette.divider: Int
  get() = separator
