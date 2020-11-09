package press.navigation

import android.view.View
import me.saket.press.shared.ui.ScreenKey

interface ScreenTransition {
  fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    goingForward: Boolean,
    onComplete: () -> Unit = {}
  ): TransitionResult

  enum class TransitionResult {
    Handled,
    Ignored
  }
}
