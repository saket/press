package compose.theme

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

class AppTheme(val palette: ThemePalette) {

  fun apply(activity: Activity) {
    activity.window.apply {
      setBackgroundDrawable(ColorDrawable(palette.window.backgroundColor))
      addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
      statusBarColor = palette.primaryColorDark
    }
  }
}

fun color(hex: String) = Color.parseColor(hex)
