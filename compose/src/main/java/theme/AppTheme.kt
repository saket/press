package theme

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import androidx.annotation.ColorInt

abstract class AppTheme(
  @ColorInt val primaryColor: Int,
  @ColorInt val primaryColorDark: Int,
  @ColorInt val accentColor: Int,
  private val window: WindowTheme
) {

  data class WindowTheme(@ColorInt val backgroundColor: Int)

  fun apply(activity: Activity) {
    activity.window.apply {
      setBackgroundDrawable(ColorDrawable(window.backgroundColor))
      addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
      statusBarColor = primaryColorDark
    }
  }
}

fun color(hex: String) = Color.parseColor(hex)
