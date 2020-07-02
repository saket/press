package press.widgets

import android.content.Context
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import press.widgets.DisplayUnit.Dp
import press.widgets.DisplayUnit.Sp

private sealed class DisplayUnit(
  private val value: Float,
  private val unit: Int
) {

  fun px(context: Context): Float {
    val metrics = context.resources.displayMetrics
    return TypedValue.applyDimension(unit, value, metrics)
  }

  data class Dp(private val value: Float) : DisplayUnit(value, COMPLEX_UNIT_DIP)
  data class Sp(private val value: Float) : DisplayUnit(value, COMPLEX_UNIT_SP)
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

fun View.dp(value: Int): Int {
  return Dp(value.toFloat()).px(context).toInt()
}

fun View.sp(value: Float): Float {
  return Sp(value).px(context)
}
