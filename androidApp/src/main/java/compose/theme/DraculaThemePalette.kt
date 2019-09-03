package compose.theme

val COLOR_BLACK_ROCK = color("#2F313F")
val COLOR_TUNA = color("#353846")
val COLOR_SCREAMING_GREEN = color("#50FA7B")
val COLOR_GHOST = color("#c2c3c7")
val COLOR_ANAKIWA = color("#8be8fd")

object DraculaThemePalette : ThemePalette(
    primaryColor = COLOR_BLACK_ROCK,
    primaryColorDark = COLOR_BLACK_ROCK,
    accentColor = COLOR_SCREAMING_GREEN,
    windowTheme = WindowTheme(backgroundColor = COLOR_TUNA),
    headingColor = COLOR_SCREAMING_GREEN,
    textColorSecondary = COLOR_GHOST,
    fabColor = COLOR_ANAKIWA
)