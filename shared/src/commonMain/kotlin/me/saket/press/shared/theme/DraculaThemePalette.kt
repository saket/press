package me.saket.press.shared.theme

val COLOR_TUNA = "#353846".toColor()
val COLOR_BLACK_ROCK = "#2F323F".toColor()
val COLOR_SPRING_GREEN = "#85F589".toColor()
val COLOR_GHOST = "#c2c3c7".toColor()
val COLOR_ANAKIWA = "#8be8fd".toColor()

object DraculaThemePalette : ThemePalette(
    primaryColor = COLOR_TUNA,
    primaryColorDark = COLOR_TUNA,
    accentColor = COLOR_SPRING_GREEN,
    window = WindowTheme(
        backgroundColor = COLOR_TUNA,
        editorBackgroundColor = COLOR_BLACK_ROCK
    ),
    textColorHeading = COLOR_SPRING_GREEN,
    textColorSecondary = COLOR_GHOST,
    fabColor = COLOR_ANAKIWA
)
