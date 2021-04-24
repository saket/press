package press.navigation

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.doOnLayout
import flow.Direction
import flow.Direction.BACKWARD
import flow.Direction.REPLACE
import flow.State
import flow.TraversalCallback
import me.saket.inboxrecyclerview.page.StandaloneExpandablePageLayout
import me.saket.press.shared.ui.ScreenKey
import press.extensions.doOnCollapse
import press.extensions.doOnExpand
import press.navigation.BackPressInterceptor.InterceptResult.Ignored
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Handled
import press.widgets.dp

/**
 * Inflates screen Views in response to backstack changes.
 *
 * Keeps multiple screen Views stacked on top of each other so that they can interact
 * together. For screens of type [ExpandableScreenKey], dragging the foreground screen
 * will cause its background screen to move in sync.
 */
class ScreenKeyChanger(
  private val hostView: () -> ViewGroup,
  private val formFactor: FormFactor,
  transitions: List<ScreenTransition>
) : EnqueuingKeyChanger() {
  private val transitions = transitions + BasicTransition()
  private var previousKey: ScreenKey? = null

  override fun changeKey(
    outgoingState: State?,
    incomingState: State,
    direction: Direction,
    incomingContexts: Map<Any, Context>,
    traversalCallback: TraversalCallback,
    transitionCallback: TransitionCallback
  ) {
    val incomingKey = incomingState.getKey<ScreenKey>()

    if (outgoingState == null && direction == REPLACE) {
      // Short circuit if we would just be showing the same view again. Flow
      // intentionally calls changeKey() again on onResume() with the same values.
      // See: https://github.com/square/flow/issues/173.
      if (previousKey == incomingKey) {
        traversalCallback.onTraversalCompleted()
        transitionCallback.onTransitionCompleted()
        return
      }
    }
    previousKey = incomingKey

    if (incomingKey !is CompositeScreenKey) {
      // FYI PlaceholderScreenKey gets discarded here.
      traversalCallback.onTraversalCompleted()
      transitionCallback.onTransitionCompleted()
      return
    }

    fun findOrCreateView(key: ScreenKey): View {
      val existing = hostView().children.firstOrNull { it.screenKey<ScreenKey>() == key }
      if (existing != null) return existing

      val context = incomingContexts[key]!!
      return formFactor.createView(context, key).also {
        warnIfIdIsMissing(it)
        incomingState.restore(it)
        hostView().addView(it)
      }
    }

    val oldForegroundView = hostView().children.lastOrNull()
    val newBackgroundView = incomingKey.background?.let(::findOrCreateView)
    val newForegroundView = incomingKey.foreground.let(::findOrCreateView)

    // The incoming or outgoing View must be drawn last.
    newForegroundView.bringToFront()
    if (direction == BACKWARD) {
      oldForegroundView?.bringToFront()
    }
    dispatchFocusChangeCallback()
    assignElevationAsPerZIndex()

    val leftOverViews = hostView().children.filter { it !== newBackgroundView && it !== newForegroundView }
    val removeLeftOverViews = {
      leftOverViews.forEach {
        outgoingState?.save(it)
        hostView().removeView(it)
      }
      dispatchFocusChangeCallback()
    }

    // When animating forward, the background View can be discarded immediately.
    // When animating backward, the foreground View is discarded after the transition.
    val isForwardTransition = direction != BACKWARD
    val onTransitionEnd = {
      if (isForwardTransition.not()) {
        removeLeftOverViews()
      }
      transitionCallback.onTransitionCompleted()  // Note to self: this must be called at the *end*.
    }
    if (isForwardTransition) {
      removeLeftOverViews()
    }

    val children = hostView().children.toList()
    val fromView: View? = if (isForwardTransition) children.secondLast() else children.last()
    val toView: View = if (isForwardTransition) children.last() else children.secondLast()!!

    if (fromView != null) {
      if (!isForwardTransition && newBackgroundView != null) {
        // The transition that handles this transition may not be the same class
        // that handles the background View, so all transitions must be called.
        transitions.forEach {
          it.prepareBackground(
            background = newBackgroundView,
            foreground = toView,
            foregroundKey = toView.screenKey()
          )
        }
      }

      transitions.first {
        it.transition(
          fromView = fromView,
          fromKey = fromView.screenKey(),
          toView = toView,
          toKey = toView.screenKey(),
          newBackground = newBackgroundView,
          goingForward = isForwardTransition,
          onComplete = onTransitionEnd
        ) == Handled
      }
    } else {
      onTransitionEnd()
    }

    traversalCallback.onTraversalCompleted()
  }

  private fun assignElevationAsPerZIndex() {
    val baseElevation = hostView().dp(40f)
    hostView().children.forEachIndexed { index, view ->
      view.elevation = baseElevation + index
    }
  }

  private fun <T> List<T>.secondLast(): T? {
    return if (size >= 2) this[lastIndex - 1] else null
  }

  private fun warnIfIdIsMissing(incomingView: View) {
    check(incomingView.id != View.NO_ID) {
      "${incomingView::class.simpleName} needs an ID for persisting View state."
    }
  }

  private fun dispatchFocusChangeCallback() {
    val children = hostView().children
      .map { formFactor.findDecoratedScreenView(it) }
      .toList()
    val foregroundView = children.lastOrNull()
    val focusedScreenKey = foregroundView?.screenKey<ScreenKey>()

    children.forEach {
      if (it !== foregroundView) {
        it.clearFocus()
      }
      if (it is ScreenFocusChangeListener) {
        it.onScreenFocusChanged(focusedScreen = focusedScreenKey)
      }
    }
  }

  fun onInterceptBackPress(): BackPressInterceptor.InterceptResult {
    val foreground = hostView().children
      .map { formFactor.findDecoratedScreenView(it) }
      .lastOrNull()

    val interceptor = foreground as? BackPressInterceptor
    return interceptor?.onInterceptBackPress() ?: return Ignored
  }
}

private class BasicTransition : ScreenTransition {
  override fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    newBackground: View?,
    goingForward: Boolean,
    onComplete: () -> Unit
  ): TransitionResult {
    if (goingForward && toView is StandaloneExpandablePageLayout) {
      toView.doOnLayout {
        val toLocation = toView.locationOnScreen()
        toView.expandFrom(Rect(toLocation.left, fromView.dp(56), toLocation.right, fromView.dp(56)))
        toView.doOnExpand(onComplete)
      }
    } else if (!goingForward && fromView is StandaloneExpandablePageLayout) {
      val fromLocation = fromView.locationOnScreen()
      fromView.collapseTo(Rect(fromLocation.left, fromView.dp(56), fromLocation.right, fromView.dp(56)))
      fromView.doOnCollapse(onComplete)
    } else {
      onComplete()
    }
    return Handled
  }
}
