package me.saket.press.shared.theme

import me.saket.wysiwyg.style.parseColor

private val COLOR_WHITE = "#FFFFFF".parseColor()
private val COLOR_TUNA = "#353846".parseColor()
private val COLOR_BLACK_ROCK = "#2F323F".parseColor()
private val COLOR_SPRING_GREEN = "#85F589".parseColor()
private val COLOR_GHOST = "#C2C3C7".parseColor()
private val COLOR_ANAKIWA = "#8BE8FD".parseColor()
private val COLOR_PERFUME = "#CCAEF9".parseColor()
private val COLOR_PERFUME_TRANSLUCENT = "#7FC9B0FF".parseColor()

object DraculaThemePalette : ThemePalette(
  isLightTheme = false,
  primaryColor = COLOR_TUNA,
  primaryColorDark = COLOR_TUNA,
  accentColor = COLOR_PERFUME,
  window = WindowPalette(
    backgroundColor = COLOR_TUNA,
    editorBackgroundColor = COLOR_BLACK_ROCK
  ),
  markdown = MarkdownPalette(
    blockQuoteVerticalRuleColor = "#CCAEF9".parseColor(),
    blockQuoteTextColor = "#CED2F8".parseColor(),
    codeBackgroundColor = "#5C121321".parseColor(),
    headingTextColor = COLOR_SPRING_GREEN,
    linkTextColor = "#8DF0FF".parseColor(),
    linkUrlColor = "#AAC6D1FF".parseColor(),
    strikethroughTextColor = "#9E9E9E".parseColor(),
    syntaxColor = COLOR_PERFUME,
    thematicBreakColor = "#62677C".parseColor()
  ),
  textHighlightColor = COLOR_PERFUME_TRANSLUCENT,
  textColorHeading = COLOR_SPRING_GREEN,
  textColorPrimary = COLOR_WHITE,
  textColorSecondary = COLOR_GHOST,
  textColorHint = "#97999F".parseColor(),
  fabColor = COLOR_ANAKIWA
)
