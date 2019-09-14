package compose.animation

import android.content.Context
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator

private var linearOutSlowIn: Interpolator? = null

fun linearOutSlowInInterpolator(context: Context): Interpolator {
  if (linearOutSlowIn == null) {
    linearOutSlowIn = AnimationUtils.loadInterpolator(
        context,
        android.R.interpolator.linear_out_slow_in
    )!!
  }
  return linearOutSlowIn!!
}