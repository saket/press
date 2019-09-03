package compose.theme

import android.graphics.Color

abstract class AppTheme(
  val primaryColor: Int,
  val primaryColorDark: Int,
  val accentColor: Int,
  val windowTheme: WindowTheme,
  val headingColor: Int,
  val textColorSecondary: Int
) {
  data class WindowTheme(val backgroundColor: Int)
}

fun color(hex: String) = Color.parseColor(hex)
