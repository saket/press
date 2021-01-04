package press.navigation

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type
import me.saket.press.shared.ui.ScreenKey
import press.extensions.hideKeyboard

interface ScreenTransition {
  fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    newBackground: View?,
    goingForward: Boolean,
    onComplete: () -> Unit = {}
  ): TransitionResult

  /**
   * Called during a back navigation, when the background View has been created and may need to be setup.
   * The transition handling the back navigation may not be the owner of the background View so this call
   * is offered to all transitions.
   */
  fun prepareBackground(
    background: View,
    foreground: View,
    foregroundKey: ScreenKey
  ) = Unit

  enum class TransitionResult {
    Handled,
    Ignored
  }
}

inline fun View.hideKeyboardAndRun(crossinline action: () -> Unit) {
  val insets = ViewCompat.getRootWindowInsets(this)?.getInsets(Type.ime())
  val isKeyboardVisible = if (insets == null) false else insets.bottom > 0

  if (isKeyboardVisible) {
    doOnHeightChange(action)
    hideKeyboard()

  } else {
    action()
  }
}

inline fun View.doOnHeightChange(crossinline action: () -> Unit) {
  addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
    override fun onLayoutChange(
      view: View,
      left: Int,
      top: Int,
      right: Int,
      bottom: Int,
      oldLeft: Int,
      oldTop: Int,
      oldRight: Int,
      oldBottom: Int
    ) {
      if ((oldBottom - oldTop) != (bottom - top)) {
        view.removeOnLayoutChangeListener(this)
        action()
      }
    }
  })
}
