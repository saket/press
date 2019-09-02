package compose.theme

abstract class ThemePalette(
  val primaryColor: Int,
  val primaryColorDark: Int,
  val accentColor: Int,
  val window: Window,
  val headingColor: Int,
  val textColorSecondary: Int
) {

  data class Window(val backgroundColor: Int)
}