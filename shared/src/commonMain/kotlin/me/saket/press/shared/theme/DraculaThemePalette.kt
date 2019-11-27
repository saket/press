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
    window = WindowTheme(
        backgroundColor = COLOR_TUNA,
        editorBackgroundColor = COLOR_BLACK_ROCK
    ),
    textColorHeading = COLOR_SPRING_GREEN,
    textColorPrimary = COLOR_WHITE,
    textColorSecondary = COLOR_GHOST,
    textHighlightColor = COLOR_DELUGE,
    fabColor = COLOR_ANAKIWA
)
