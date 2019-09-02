package compose.theme

val COLOR_SHARK = color("#363846")
val COLOR_SCREAMING_GREEN = color("#50FA7B")
val GHOST = color("#c2c3c7")

object DraculaThemePalette : ThemePalette(
    primaryColor = COLOR_SHARK,
    primaryColorDark = COLOR_SHARK,
    accentColor = COLOR_SCREAMING_GREEN,
    window = Window(backgroundColor = COLOR_SHARK),
    headingColor = COLOR_SCREAMING_GREEN,
    textColorSecondary = GHOST
)