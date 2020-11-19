package press.navigation.transitions

import android.graphics.Color.BLACK
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.ime
import androidx.core.view.doOnNextLayout
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialContainerTransform.FADE_MODE_OUT
import com.google.android.material.transition.MaterialContainerTransform.ProgressThresholds
import me.saket.press.shared.ui.ScreenKey
import press.extensions.findChild
import press.extensions.hideKeyboard
import press.extensions.withOpacity
import press.navigation.ScreenTransition
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Handled
import press.navigation.ScreenTransition.TransitionResult.Ignored

class MorphFromFabScreenTransition : ScreenTransition {
  override fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    goingForward: Boolean,
    onComplete: () -> Unit
  ): TransitionResult {
    if (goingForward && toKey is MorphFromFabScreenKey) {
      val fab = fromView.findChild<FloatingActionButton>()!!
      val transform = fabMorphTransition(from = fab, to = toView, onComplete = onComplete)
      TransitionManager.beginDelayedTransition(fromView.parent as ViewGroup, transform)
      return Handled

    } else if (!goingForward && fromKey is MorphFromFabScreenKey) {
      val fab = toView.findChild<FloatingActionButton>()!!
      val transform = fabMorphTransition(from = fromView, to = fab, onComplete = onComplete)
      fromView.hideKeyboardAndRun {
        // If the keyboard is visible, the screen will morph into the FAB
        // above the keyboard and then immediately jump to the bottom. Avoid
        // this by hiding the keyboard and before starting the transition.
        TransitionManager.beginDelayedTransition(fromView.parent as ViewGroup, transform)
      }
      return Handled

    } else {
      return Ignored
    }
  }
}

private inline fun View.hideKeyboardAndRun(crossinline action: () -> Unit) {
  val insets = ViewCompat.getRootWindowInsets(this)?.getInsets(ime())
  val isKeyboardVisible = if (insets == null) false else insets.bottom > 0

  if (isKeyboardVisible) {
    hideKeyboard()
    doOnNextLayout { action() }

  } else {
    action()
  }
}

private fun fabMorphTransition(from: View, to: View, onComplete: () -> Unit): Transition {
  return MaterialContainerTransform().apply {
    startView = from
    endView = to
    duration = 400
    fadeMode = FADE_MODE_OUT
    scrimColor = BLACK.withOpacity(0.25f) // Same as InboxRecyclerView's dim painter.

    addTarget(to)
    setPathMotion(MaterialArcMotion())

    shapeMaskProgressThresholds = ProgressThresholds(0.3f, 0.9f)
    scaleProgressThresholds = ProgressThresholds(0.2f, 0.9f)
    scaleMaskProgressThresholds = ProgressThresholds(0.2f, 0.9f)

    addListener(object : TransitionListenerAdapter() {
      override fun onTransitionEnd(transition: Transition) = onComplete()
    })
  }
}
