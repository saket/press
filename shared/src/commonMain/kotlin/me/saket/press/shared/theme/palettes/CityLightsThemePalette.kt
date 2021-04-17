package me.saket.press.shared.theme.palettes

import me.saket.press.shared.theme.lightenBy
import me.saket.press.shared.theme.toHsvColor
import me.saket.press.shared.theme.toRgbColorInt
import me.saket.press.shared.theme.withAlpha
import me.saket.wysiwyg.style.parseColor

private val BACKGROUND = "#1A1F25".parseColor()
private val ACCENT_COLOR = "#6CEEA9".parseColor()
private val HEADING = "#33ced8".parseColor()
private val TEXT_COLOR = "#A0B3C6".parseColor()
private val LINK = "#F0BE72".parseColor()

object CityLightsThemePalette : ThemePalette(
    name = "City lights",
    isLightTheme = false,
    primaryColor = BACKGROUND,
    primaryColorDark = BACKGROUND,
    accentColor = ACCENT_COLOR,
    window = WindowPalette(
        backgroundColor = BACKGROUND,
        elevatedBackgroundColor = "#1D252C".parseColor()
    ),
    markdown = MarkdownPalette(
        syntaxColor = "#60788b".parseColor(),
        blockQuoteTextColor = "#CED2F8".parseColor(),
        linkTextColor = ACCENT_COLOR,
        linkUrlColor = "#AAC6D1FF".parseColor(),
        codeBackgroundColor = "#2B3946".parseColor().withAlpha(0.36f),
        thematicBreakColor = BACKGROUND.toHsvColor().lightenBy(0.81f).toRgbColorInt()
    ),
    textColorHeading = HEADING,
    textColorPrimary = TEXT_COLOR,
    textColorWarning = "#FF9580".parseColor(),
    fabColor = LINK
)
