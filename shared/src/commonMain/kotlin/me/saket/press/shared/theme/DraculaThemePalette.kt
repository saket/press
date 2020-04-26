package me.saket.press.shared.theme

import me.saket.wysiwyg.style.parseColor

val COLOR_WHITE = "#FFFFFF".parseColor()
val COLOR_TUNA = "#353846".parseColor()
val COLOR_BLACK_ROCK = "#2F323F".parseColor()
val COLOR_SPRING_GREEN = "#85F589".parseColor()
val COLOR_GHOST = "#C2C3C7".parseColor()
val COLOR_ANAKIWA = "#8BE8FD".parseColor()
val COLOR_DELUGE = "#7FC9B0FF".parseColor()

object DraculaThemePalette : ThemePalette(
    primaryColor = COLOR_TUNA,
    primaryColorDark = COLOR_TUNA,
    accentColor = COLOR_SPRING_GREEN,
    textHighlightColor = COLOR_DELUGE,
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
        syntaxColor = "#CCAEF9".parseColor(),
        thematicBreakColor = "#62677C".parseColor()
    ),
    textColorHeading = COLOR_SPRING_GREEN,
    textColorPrimary = COLOR_WHITE,
    textColorSecondary = COLOR_GHOST,
    fabColor = COLOR_ANAKIWA
)
