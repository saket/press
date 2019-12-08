package me.saket.press.shared.theme

val COLOR_WHITE = "#FFFFFF".toColor()
val COLOR_TUNA = "#353846".toColor()
val COLOR_BLACK_ROCK = "#2F323F".toColor()
val COLOR_SPRING_GREEN = "#85F589".toColor()
val COLOR_GHOST = "#C2C3C7".toColor()
val COLOR_ANAKIWA = "#8BE8FD".toColor()
val COLOR_DELUGE = "#7FC9B0FF".toColor()

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
        syntaxColor = "#CCAEF9".toColor(),
        blockQuoteVerticalRuleColor = "#CCAEF9".toColor(),
        blockQuoteTextColor = "#CED2F8".toColor(),
        linkUrlColor = "#AAC6D1FF".toColor(),
        linkTextColor = "#8DF0FF".toColor(),
        thematicBreakColor = "#62677C".toColor(),
        codeBackgroundColor = "#242632".toColor(),
        headingTextColor = COLOR_SPRING_GREEN
    ),
    textColorHeading = COLOR_SPRING_GREEN,
    textColorPrimary = COLOR_WHITE,
    textColorSecondary = COLOR_GHOST,
    fabColor = COLOR_ANAKIWA
)
