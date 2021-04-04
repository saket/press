package me.saket.press.shared.theme

import me.saket.wysiwyg.style.parseColor

private val TEXT_COLOR = "#FFFFFF".parseColor()
private val BACKGROUND = "#353846".parseColor()
private val HEADING = "#85F589".parseColor()
private val LINK = "#8DF0FF".parseColor()
private val ACCENT_COLOR = "#CCAEF9".parseColor()

object DraculaThemePalette : ThemePalette(
  name = "Dracula",
  isLightTheme = false,
  primaryColor = BACKGROUND,
  primaryColorDark = BACKGROUND,
  accentColor = ACCENT_COLOR,
  window = WindowPalette(backgroundColor = BACKGROUND),
  markdown = MarkdownPalette(
    blockQuoteTextColor = "#CED2F8".parseColor(),
    linkTextColor = LINK,
    linkUrlColor = "#AAC6D1FF".parseColor(),
    thematicBreakColor = BACKGROUND.toHsvColor().lightenBy(0.81f).toRgbColorInt()
  ),
  textColorHeading = HEADING,
  textColorPrimary = TEXT_COLOR,
  textColorWarning = "#FF9580".parseColor(),
  fabColor = LINK
)
