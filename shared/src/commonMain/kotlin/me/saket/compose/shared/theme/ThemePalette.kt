package me.saket.compose.shared.theme

abstract class ThemePalette(
  val primaryColor: Int,
  val primaryColorDark: Int,
  val accentColor: Int,
  val windowTheme: WindowTheme,
  val headingColor: Int,
  val textColorSecondary: Int,
  val fabColor: Int
) {
  data class WindowTheme(val backgroundColor: Int)
}
