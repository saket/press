package press.navigation

import android.view.View
import me.saket.press.shared.ui.ScreenKey

interface ScreenTransition {
  /**
   * Called when navigating back, when the background View
   * needs to be prepared for a future screen transition.
   */
  fun prepareBackground(
    background: View,
    foreground: View,
    foregroundKey: ScreenKey
  ) = Unit

  fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    newBackground: View?,
    goingForward: Boolean,
    onComplete: () -> Unit = {}
  ): TransitionResult

  enum class TransitionResult {
    Handled,
    Ignored
  }
}
