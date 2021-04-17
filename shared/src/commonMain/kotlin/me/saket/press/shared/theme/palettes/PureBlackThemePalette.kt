package me.saket.press.shared.theme.palettes

import me.saket.press.shared.theme.darkenBy
import me.saket.press.shared.theme.increaseHueBy
import me.saket.press.shared.theme.lightenBy
import me.saket.press.shared.theme.saturateBy
import me.saket.press.shared.theme.toHsvColor
import me.saket.press.shared.theme.toRgbColorInt
import me.saket.wysiwyg.style.parseColor

private val BACKGROUND = "#000000".parseColor()
private val ACCENT_COLOR = "#CCAEF9".parseColor()
private val HEADING = "#FAD121".parseColor()
private val TEXT_COLOR = "#FFFFFF".parseColor()
private val LINK = "#5E532A".parseColor()

object PureBlackThemePalette : ThemePalette(
    name = "Pure black",
    isLightTheme = false,
    primaryColor = BACKGROUND,
    primaryColorDark = BACKGROUND,
    accentColor = ACCENT_COLOR,
    window = WindowPalette(
        backgroundColor = BACKGROUND,
        elevatedBackgroundColor = "#111111".parseColor()
    ),
    markdown = MarkdownPalette(
        syntaxColor = "#644004".parseColor(),
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
