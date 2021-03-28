package press.widgets.insets

import android.view.View
import android.view.ViewGroup
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.WindowInsetsCompat.Type.systemBars

/**
 * Synchronizes keyboard entry & exit animation with the app's layout.
 *
 * Google's [official sample](https://github.com/android/user-interface-samples/tree/main/WindowInsetsAnimation)
 * prefers to move each UI component individually by animating their translationY for maximum efficiency. Press takes
 * a shortcut by updating the padding for the entire app on every frame of the animation. This isn't ideal, but it
 * makes the whole logic extremely simple to maintain instead of doing it in every screen.
 */
class KeyboardInsetsChangeAnimator(
  private val layout: ViewGroup
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE),
  OnApplyWindowInsetsListener {

  private lateinit var lastWindowInsets: WindowInsetsCompat
  private var isKeyboardAnimating = false

  // Press defers applying of keyboard insets so that they can be smoothly animated.
  // This flag is used when the insets need to be applied immediately, for example,
  // during screen transitions.
  var isTemporarilyDisabled = false

  override fun onPrepare(animation: WindowInsetsAnimationCompat) {
    if (!isTemporarilyDisabled && animation.typeMask and ime() != 0) {
      isKeyboardAnimating = true
    }
  }

  override fun onApplyWindowInsets(view: View, insets: WindowInsetsCompat): WindowInsetsCompat {
    lastWindowInsets = insets

    // When the keyboard isn't animating, the insets are applied immediately.
    // Otherwise, they're applied during each frame of the animation in onProgress().
    if (!isKeyboardAnimating) {
      layout.setPadding(insets)
    }

    // Stop the insets being dispatched any further into the view hierarchy.
    return WindowInsetsCompat.CONSUMED
  }

  override fun onProgress(
    insets: WindowInsetsCompat,
    runningAnimations: List<WindowInsetsAnimationCompat>
  ): WindowInsetsCompat {
    if (isKeyboardAnimating) {
      layout.setPadding(insets)
    }
    return insets
  }

  override fun onEnd(animation: WindowInsetsAnimationCompat) {
    if (isKeyboardAnimating && (animation.typeMask and ime() != 0)) {
      isKeyboardAnimating = false
    }
    isTemporarilyDisabled = false
  }

  private fun View.setPadding(windowInsets: WindowInsetsCompat) {
    windowInsets.getInsets(systemBars() or ime()).let {
      setPadding(it.left, it.top, it.right, it.bottom)
    }
  }
}
