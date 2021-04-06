package me.saket.press.shared.theme.palettes

import me.saket.press.shared.theme.MarkdownPalette
import me.saket.press.shared.theme.ThemePalette
import me.saket.press.shared.theme.WindowPalette
import me.saket.press.shared.theme.darkenBy
import me.saket.press.shared.theme.increaseHueBy
import me.saket.press.shared.theme.lightenBy
import me.saket.press.shared.theme.saturateBy
import me.saket.press.shared.theme.toHsvColor
import me.saket.press.shared.theme.toRgbColorInt
import me.saket.wysiwyg.style.parseColor

private val BACKGROUND = "#E7FBF1".parseColor()
private val ACCENT_COLOR = "#547B80".parseColor()
private val HEADING = "#356859".parseColor()
private val TEXT_COLOR = "#356859".parseColor()
private val LINK = "#CE3100".parseColor()

object CascadeThemePalette : ThemePalette(
  name = "Cascade",
  isLightTheme = true,
  primaryColor = BACKGROUND,
  primaryColorDark = BACKGROUND,
  accentColor = ACCENT_COLOR,
  window = WindowPalette(
    backgroundColor = BACKGROUND,
    elevatedBackgroundColor = "#C7EFDB".parseColor()
  ),
  markdown = MarkdownPalette(
    syntaxColor = "#547B80".parseColor(),
    blockQuoteTextColor = "#CED2F8".parseColor(),
    linkTextColor = LINK,
    linkUrlColor = "#AAC6D1FF".parseColor(),
    codeBackgroundColor = BACKGROUND.toHsvColor()
      .darkenBy(0.52f)
      .saturateBy(0.875f)
      .increaseHueBy(0.03f)
      .copy(a = 0.36f)
      .toRgbColorInt(),
    thematicBreakColor = BACKGROUND.toHsvColor().lightenBy(0.81f).toRgbColorInt()
  ),
  textColorHeading = HEADING,
  textColorPrimary = TEXT_COLOR,
  textColorWarning = "#FF9580".parseColor(),
  fabColor = LINK
)
