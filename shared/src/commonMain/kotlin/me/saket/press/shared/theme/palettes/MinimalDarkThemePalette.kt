package me.saket.press.shared.theme.palettes

import me.saket.press.shared.theme.lightenBy
import me.saket.press.shared.theme.toHsvColor
import me.saket.press.shared.theme.toRgbColorInt
import me.saket.press.shared.theme.withAlpha
import me.saket.wysiwyg.style.parseColor

private val BACKGROUND = "#29292b".parseColor()
private val ACCENT_COLOR = "#E37A7D".parseColor()
private val HEADING = "#FFFFFF".parseColor()
private val TEXT_COLOR = "#FFFFFF".parseColor()
private val LINK = "#4CE574".parseColor()

object MinimalDarkThemePalette : ThemePalette(
    name = "Minimal dark",
    isLightTheme = false,
    primaryColor = BACKGROUND,
    primaryColorDark = BACKGROUND,
    accentColor = ACCENT_COLOR,
    window = WindowPalette(
        backgroundColor = BACKGROUND,
        elevatedBackgroundColor = "#29292b".parseColor()
    ),
    markdown = MarkdownPalette(
        syntaxColor = "#E37A7D".parseColor(),
        blockQuoteTextColor = "#CED2F8".parseColor(),
        linkTextColor = "#E37A7D".parseColor(),
        linkUrlColor = "#8473A5".parseColor(),
        codeBackgroundColor = "#484D70".parseColor().withAlpha(0.36f),
        thematicBreakColor = BACKGROUND.toHsvColor().lightenBy(0.81f).toRgbColorInt()
    ),
    textColorHeading = HEADING,
    textColorPrimary = TEXT_COLOR,
    textColorWarning = "#FF9580".parseColor(),
    fabColor = LINK
)
