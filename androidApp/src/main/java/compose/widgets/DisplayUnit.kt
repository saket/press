package compose.widgets

import android.content.Context
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import compose.widgets.DisplayUnit.Dp

private sealed class DisplayUnit(
  private val value: Float,
  private val unit: Int
) {

  fun px(context: Context): Float {
    val metrics = context.resources.displayMetrics
    return TypedValue.applyDimension(unit, value, metrics)
  }

  data class Dp(private val value: Float) : DisplayUnit(value, COMPLEX_UNIT_DIP)
}

fun Context.dp(value: Int): Int {
  return Dp(value.toFloat()).px(this).toInt()
}

fun Context.dp(value: Float): Float {
  return Dp(value).px(this)
}

fun View.dp(value: Float): Float {
  return Dp(value).px(context)
}