package me.saket.press.shared.theme.palettes

import me.saket.press.shared.theme.ThemePalette
import me.saket.press.shared.theme.lightenBy
import me.saket.press.shared.theme.toHsvColor
import me.saket.press.shared.theme.toRgbColorInt
import me.saket.press.shared.theme.withAlpha
import me.saket.wysiwyg.style.parseColor

private val BACKGROUND = "#FFFFFF".parseColor()
private val ACCENT_COLOR = "#E37A7D".parseColor()
private val HEADING = "#2A2D2F".parseColor()
private val TEXT_COLOR = "#2A2D2F".parseColor()
private val LINK = "#D84246".parseColor()

object MinimalLightThemePalette : ThemePalette(
  name = "Minimal light",
  isLightTheme = true,
  primaryColor = BACKGROUND,
  primaryColorDark = BACKGROUND,
  accentColor = ACCENT_COLOR,
  window = WindowPalette(
    backgroundColor = BACKGROUND,
    elevatedBackgroundColor = "#F7F7F7".parseColor()
  ),
  markdown = MarkdownPalette(
    syntaxColor = "#E37A7D".parseColor(),
    blockQuoteTextColor = "#CED2F8".parseColor(),
    linkTextColor = LINK,
    linkUrlColor = "#AAC6D1FF".parseColor(),
    codeBackgroundColor = "#ACB2C0".parseColor().withAlpha(0.36f),
    thematicBreakColor = BACKGROUND.toHsvColor().lightenBy(0.81f).toRgbColorInt()
  ),
  textColorHeading = HEADING,
  textColorPrimary = TEXT_COLOR,
  textColorWarning = "#FF9580".parseColor(),
  fabColor = LINK
)
