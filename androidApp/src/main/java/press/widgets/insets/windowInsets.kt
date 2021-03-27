package press.widgets.insets

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsAnimationCompat.BoundsCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.ime

fun View.keyboardHeight(): Int? {
  return ViewCompat.getRootWindowInsets(this)?.getInsets(ime())?.bottom
}

fun View.isKeyboardVisible(): Boolean {
  val insets = ViewCompat.getRootWindowInsets(this)?.getInsets(ime())
  return if (insets == null) false else insets.bottom > 0
}

inline fun View.doOnNextKeyboardVisibilityChange(crossinline onStart: () -> Unit) {
  ViewCompat.setWindowInsetsAnimationCallback(
    this,
    object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
      override fun onStart(animation: WindowInsetsAnimationCompat, bounds: BoundsCompat): BoundsCompat {
        if (animation.typeMask and ime() != 0) {
          onStart()
          ViewCompat.setWindowInsetsAnimationCallback(this@doOnNextKeyboardVisibilityChange, null)
        }
        return super.onStart(animation, bounds)
      }

      override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>
      ) = insets
    })
}
