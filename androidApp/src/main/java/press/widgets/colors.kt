package press.widgets

import android.content.res.ColorStateList

/**
 * Usage:
 *
 * ```
 * import android.R.attr.state_selected
 * import android.R.attr.state_enabled
 *
 * colorStateListOf(
 *   intArrayOf(state_selected, state_enabled) to Color.YELLOW,
 *   intArrayOf(state_selected) to Color.BLACK,
 *   intArrayOf() to Color.WHITE
 * )
 * ```
 */
fun colorStateListOf(vararg mapping: Pair<IntArray, Int>): ColorStateList {
  val (states, colors) = mapping.unzip()
  return ColorStateList(states.toTypedArray(), colors.toIntArray())
}
