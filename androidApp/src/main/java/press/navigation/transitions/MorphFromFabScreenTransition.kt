package press.navigation.transitions

import android.graphics.Color.TRANSPARENT
import android.view.View
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialContainerTransform.FADE_MODE_OUT
import com.google.android.material.transition.MaterialContainerTransform.ProgressThresholds
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.dimming.AnimatedVisibilityColorDrawable
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState.COLLAPSING
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState.EXPANDED
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState.EXPANDING
import me.saket.inboxrecyclerview.page.StandaloneExpandablePageLayout
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.ui.ScreenKey
import press.extensions.findChild
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
      fromList.initPageDimming(parent = fromView, page = toView, pageState = EXPANDED)
      toView.expandImmediately()

      val fab = fromView.findChild<FloatingActionButton>()!!
      val transform = fabMorphTransition(from = fab, to = toView)
      beginOrCompleteTransition(fromView.parent as ViewGroup, transform, onComplete = onComplete)
      return Handled

    } else if (isNewNoteScreen(fromView, fromKey) && toKey is HomeScreenKey) {
      val toList = toView.findChild<InboxRecyclerView>()!!
      toList.initPageDimming(parent = toView, page = fromView, pageState = COLLAPSING)

      val fab = toView.findChild<FloatingActionButton>()!!
      val transform = fabMorphTransition(from = fromView, to = fab)

      fromView.hideKeyboardAndRun {
        // If the keyboard is visible, the screen will morph into the FAB
        // above the keyboard and then immediately jump to the bottom. Avoid
        // this by hiding the keyboard and before starting the transition.
        beginOrCompleteTransition(fromView.parent as ViewGroup, transform, onComplete = {
          toList.resetPageDimming()
          onComplete()
        })
      }
      return Handled

    } else {
      return Ignored
    }
  }

  private fun beginOrCompleteTransition(parent: ViewGroup, transition: Transition, onComplete: () -> Unit) {
    // Transitions are only run if Views are laid out.
    if (parent.isLaidOut) {
      transition.addListener(object : TransitionListenerAdapter() {
        override fun onTransitionEnd(transition: Transition) = onComplete()
      })
      TransitionManager.beginDelayedTransition(parent, transition)

    } else {
      onComplete()
    }
  }

  @OptIn(ExperimentalContracts::class)
  private fun isNewNoteScreen(view: View, key: ScreenKey): Boolean {
    contract {
      returns() implies (view is StandaloneExpandablePageLayout)
    }
    return (key as? EditorScreenKey)?.openMode is NewNote
  }

  private fun InboxRecyclerView.initPageDimming(page: ExpandablePageLayout, parent: View, pageState: PageState) {
    this.dimPainter = MorphingScreenDimPainter(
      // These foregrounds are set in PhoneFormFactor.
      listDrawable = parent.foreground as AnimatedVisibilityColorDrawable,
      pageDrawable = page.foreground as AnimatedVisibilityColorDrawable,
      fixedPageState = pageState
    )

    // This expandable page can't directly be wired to the host InboxRecyclerView
    // otherwise it'll try to control its lifecycle including its collapse animation.
    this.dimPainter.onAttachRecyclerView(this, page)
  }

  private fun InboxRecyclerView.resetPageDimming() {
    this.dimPainter.onDetachRecyclerView(resetDim = true)
  }

  private fun fabMorphTransition(from: View, to: View): Transition {
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
    }
  }
}

private class MorphingScreenDimPainter(
  listDrawable: AnimatedVisibilityColorDrawable,
  pageDrawable: AnimatedVisibilityColorDrawable,
  private val fixedPageState: PageState
) : ExpandableScreenDimPainter(listDrawable, pageDrawable) {

  override fun pageState(page: ExpandablePageLayout) = fixedPageState
}
