package press.navigation

import android.view.View
import me.saket.press.shared.ui.ScreenKey
import press.extensions.findParent
import press.extensions.hideKeyboard
import press.widgets.insets.doOnNextKeyboardVisibilityChange
import press.widgets.insets.isKeyboardVisible

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

  fun View.hideKeyboardAndRun(action: () -> Unit) {
    if (isKeyboardVisible()) {
      findParent<NavigationHostLayout>().applyNextInsetChangeImmediately()
      doOnNextKeyboardVisibilityChange(action)
      hideKeyboard()

    } else {
      action()
    }
  }
}
