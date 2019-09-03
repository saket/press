package compose.theme

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager.LayoutParams

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

fun AppTheme.apply(activity: Activity) {
  activity.window.apply {
    setBackgroundDrawable(ColorDrawable(windowTheme.backgroundColor))
    addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    statusBarColor = primaryColorDark
  }
}

fun color(hex: String) = Color.parseColor(hex)
