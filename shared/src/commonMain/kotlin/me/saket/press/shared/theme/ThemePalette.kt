package me.saket.press.shared.theme

abstract class ThemePalette(
  val primaryColor: Int,
  val primaryColorDark: Int,
  val accentColor: Int,
  val window: WindowTheme,
  val textColorHeading: Int,
  val textColorSecondary: Int,
  val fabColor: Int
) {
  data class WindowTheme(val backgroundColor: Int)
}
