package me.saket.compose.shared.theme

val COLOR_BLACK_ROCK = "#2F313F".toColor()
val COLOR_TUNA = "#353846".toColor()
val COLOR_SCREAMING_GREEN = "#50FA7B".toColor()
val COLOR_GHOST = "#c2c3c7".toColor()
val COLOR_ANAKIWA = "#8be8fd".toColor()

object DraculaThemePalette : ThemePalette(
    primaryColor = COLOR_BLACK_ROCK,
    primaryColorDark = COLOR_BLACK_ROCK,
    accentColor = COLOR_SCREAMING_GREEN,
    windowTheme = WindowTheme(backgroundColor = COLOR_TUNA),
    headingColor = COLOR_SCREAMING_GREEN,
    textColorSecondary = COLOR_GHOST,
    fabColor = COLOR_ANAKIWA
)