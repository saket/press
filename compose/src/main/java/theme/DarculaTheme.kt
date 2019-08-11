package theme

object Palette {
  val COLOR_SHARK = color("#363846")
  val COLOR_SCREAMING_GREEN = color("#50FA7B")
}

object DarculaTheme : AppTheme(
    primaryColor = Palette.COLOR_SHARK,
    primaryColorDark = Palette.COLOR_SHARK,
    accentColor = Palette.COLOR_SCREAMING_GREEN,
    window = WindowTheme(backgroundColor = Palette.COLOR_SHARK)
)