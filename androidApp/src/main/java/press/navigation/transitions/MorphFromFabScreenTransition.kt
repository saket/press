package press.navigation.transitions

import android.graphics.Color.TRANSPARENT
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
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.StandaloneExpandablePageLayout
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.ui.ScreenKey
import press.extensions.findChild
import press.extensions.hideKeyboard
import press.navigation.ScreenTransition
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Handled
import press.navigation.ScreenTransition.TransitionResult.Ignored
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class MorphFromFabScreenTransition : ScreenTransition {
  override fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    newBackground: View?,
    goingForward: Boolean,
    onComplete: () -> Unit
  ): TransitionResult {
    if (fromKey is HomeScreenKey && isNewNoteScreen(toView, toKey)) {
      val fromList = fromView.findChild<InboxRecyclerView>()!!
      fromList.startPageDimming(toView)
      toView.expandImmediately()

      val fab = fromView.findChild<FloatingActionButton>()!!
      val transform = fabMorphTransition(from = fab, to = toView, onComplete = onComplete)
      TransitionManager.beginDelayedTransition(fromView.parent as ViewGroup, transform)
      return Handled

    } else if (isNewNoteScreen(fromView, fromKey) && toKey is HomeScreenKey) {
      val toList = toView.findChild<InboxRecyclerView>()!!
      toList.stopPageDimming()

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

  @OptIn(ExperimentalContracts::class)
  private fun isNewNoteScreen(view: View, key: ScreenKey): Boolean {
    contract {
      returns() implies (view is StandaloneExpandablePageLayout)
    }
    return (key as? EditorScreenKey)?.openMode is NewNote
  }

  private fun InboxRecyclerView.startPageDimming(page: ExpandablePageLayout) {
    // This expandable page can't directly be wired to the host InboxRecyclerView
    // otherwise will try to control its lifecycle including its collapse animation.
    this.dimPainter.onAttachRecyclerView(this, page)
  }

  private fun InboxRecyclerView.stopPageDimming() {
    this.dimPainter.onDetachRecyclerView()
    dimDrawable?.alpha = 0
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
      scrimColor = TRANSPARENT  // Scrim is painted by InboxRecyclerView.

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
}
