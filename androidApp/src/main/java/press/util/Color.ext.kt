package press.util

import android.graphics.Color
import androidx.annotation.FloatRange

fun Int.withOpacity(@FloatRange(from = 0.0, to = 1.0) opacity: Float): Int {
  val alpha = Color.alpha(this) * opacity + Color.alpha(Color.TRANSPARENT) * (1 - opacity)
  return Color.argb(
      alpha.toInt(),
      Color.red(this),
      Color.green(this),
      Color.blue(this)
  )
}