package press.navigation

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.transition.TransitionManager
import flow.Direction.REPLACE
import flow.KeyChanger
import flow.State
import flow.TraversalCallback
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.StandaloneExpandablePageLayout
import me.saket.press.shared.ui.ScreenKey
import press.extensions.findChild
import press.navigation.BackPressInterceptor.InterceptResult.Ignored
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Handled

/**
 * Inflates screen Views in response to backstack changes.
 *
 * Keeps multiple screen Views stacked on top of each other so that they can interact
 * together. For screens of type [ExpandableScreenKey], pulling a foreground screen will
 * reveal its background screen.
 */
class ScreenKeyChanger(
  private val hostView: () -> ViewGroup,
  private val formFactor: FormFactor,
  transitions: List<ScreenTransition>
) : KeyChanger {
  private val transitions = transitions + BasicTransition()
  private var previousKey: ScreenKey? = null

  override fun changeKey(
    outgoingState: State?,
    incomingState: State,
    direction: flow.Direction,
    incomingContexts: Map<Any, Context>,
    callback: TraversalCallback
  ) {
    val incomingKey = incomingState.getKey<ScreenKey>()

    if (outgoingState == null && direction == REPLACE) {
      // Short circuit if we would just be showing the same view again. Flow
      // intentionally calls changeKey() again on onResume() with the same values.
      // See: https://github.com/square/flow/issues/173.
      if (previousKey == incomingKey) {
        callback.onTraversalCompleted()
        return
      }
    }
    previousKey = incomingKey

    if (incomingKey !is CompositeScreenKey) {
      // FYI PlaceholderScreenKey gets discarded here.
      callback.onTraversalCompleted()
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
    val foregroundView = incomingKey.foreground.let(::findOrCreateView)
    foregroundView.bringToFront()
    dispatchFocusChangeCallback()

    val leftOverViews = hostView().children
      .filter { it !== newBackgroundView && it !== foregroundView }

    val onTransitionEnd = {
      leftOverViews.forEach {
        outgoingState?.save(it)
        hostView().removeView(it)
      }
      dispatchFocusChangeCallback()
    }

    val outgoingKey = outgoingState?.getKey<ScreenKey>() as? CompositeScreenKey
    val wasStateRestored = outgoingKey == null && incomingKey.background != null && direction == REPLACE

    val fromView: View? = if (wasStateRestored) newBackgroundView else oldForegroundView
    val fromKey: ScreenKey? = if (wasStateRestored) incomingKey.background else outgoingKey?.foreground

    if (fromView != null) {
      transitions.first {
        it.transition(
          fromView = fromView,
          fromKey = fromKey!!,
          toView = foregroundView,
          toKey = incomingKey.foreground,
          onComplete = onTransitionEnd
        ) == Handled
      }
    } else {
      onTransitionEnd()
    }

    callback.onTraversalCompleted()
  }

  private fun warnIfIdIsMissing(incomingView: View) {
    check(incomingView.id != View.NO_ID) {
      "${incomingView::class.simpleName} needs an ID for persisting View state."
    }
  }

  private fun dispatchFocusChangeCallback() {
    val children = hostView().children.toList()
    val foregroundView = children.lastOrNull()

    children
      .filterIsInstance<ScreenFocusChangeListener>()
      .forEach {
        it.onScreenFocusChanged(focusedScreen = foregroundView)
      }
  }

  fun onInterceptBackPress(): BackPressInterceptor.InterceptResult {
    val foreground = hostView().children.lastOrNull()
    val interceptor = (foreground as? ViewGroup)?.findChild<BackPressInterceptor>()
    return interceptor?.onInterceptBackPress() ?: return Ignored
  }
}

private class BasicTransition : ScreenTransition {
  override fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    onComplete: () -> Unit
  ): TransitionResult {
    if (toView is StandaloneExpandablePageLayout) {
      toView.expandImmediately()
    } else if (fromView is ExpandablePageLayout) {
      // Can't collapse without a parent InboxRecyclerView.
      TransitionManager.beginDelayedTransition(fromView.parent as ViewGroup)
    }
    onComplete()
    return Handled
  }
}
