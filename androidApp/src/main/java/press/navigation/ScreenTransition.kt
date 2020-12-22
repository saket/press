package press.navigation

import android.view.View
import me.saket.press.shared.ui.ScreenKey

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
