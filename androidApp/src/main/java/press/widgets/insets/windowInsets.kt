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

inline fun View.doOnKeyboardVisibilityChange(crossinline onStart: () -> Unit) {
  ViewCompat.setWindowInsetsAnimationCallback(this, object : SimpleWindowInsetsAnimationCompatCallback() {
    override fun onStart(animation: WindowInsetsAnimationCompat, bounds: BoundsCompat): BoundsCompat {
      if (animation.typeMask and ime() != 0) {
        onStart()
        ViewCompat.setWindowInsetsAnimationCallback(this@doOnKeyboardVisibilityChange, null)
      }
      return super.onStart(animation, bounds)
    }
  })
}

inline fun View.doOnPreKeyboardVisibilityChange(crossinline onPrepare: (WindowInsetsAnimationCompat) -> Unit) {
  ViewCompat.setWindowInsetsAnimationCallback(this, object : SimpleWindowInsetsAnimationCompatCallback() {
    override fun onPrepare(animation: WindowInsetsAnimationCompat) {
      if (animation.typeMask and ime() != 0) {
        onPrepare(animation)
        ViewCompat.setWindowInsetsAnimationCallback(this@doOnPreKeyboardVisibilityChange, null)
      }
    }
  })
}

abstract class SimpleWindowInsetsAnimationCompatCallback :
  WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
  override fun onProgress(
    insets: WindowInsetsCompat,
    runningAnimations: List<WindowInsetsAnimationCompat>
  ) = insets
}
