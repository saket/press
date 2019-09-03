package compose.widgets

import android.content.Context
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import compose.widgets.DisplayUnit.Dp

sealed class DisplayUnit(
  private val value: Float,
  private val unit: Int
) {

  fun px(context: Context): Float {
    val metrics = context.resources.displayMetrics
    return TypedValue.applyDimension(unit, value, metrics)
  }

  data class Dp(private val value: Float) : DisplayUnit(value, COMPLEX_UNIT_DIP)
}

val Int.dp: Dp
  get() = Dp(toFloat())

val Float.dp: Dp
  get() = Dp(this)
