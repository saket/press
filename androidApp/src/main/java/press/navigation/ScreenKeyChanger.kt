package press.navigation

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import flow.Direction.REPLACE
import flow.KeyChanger
import flow.State
import flow.TraversalCallback
import me.saket.press.shared.ui.ScreenKey
import press.navigation.BackPressInterceptor.InterceptResult.Ignored
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Handled
import press.theme.themeAware

/**
 * Inflates screen Views in response to backstack changes.
 *
 * Keeps multiple screen Views stacked on top of each other so that they can interact
 * together. For screens of type [ExpandableScreenKey], pulling a foreground screen will
 * reveal its background screen.
 */
class ScreenKeyChanger(
  private val hostView: () -> ViewGroup,
  private val viewFactories: ViewFactories,
  transitions: List<ScreenTransition>
) : KeyChanger {
  private val transitions = transitions + NoOpTransition()
  val focusChangeListeners = mutableListOf<ScreenFocusChangeListener>()

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
      if (peek() == incomingKey) {
        callback.onTraversalCompleted()
        return
      }
    }

    if (incomingKey !is CompositeScreenKey) {
      // FYI PlaceholderScreenKey gets discarded here.
      callback.onTraversalCompleted()
      return
    }

    fun findOrCreateView(key: ScreenKey): View {
      val existing = hostView().children.firstOrNull { it.screenKey<ScreenKey>() == key }
      if (existing != null) return existing

      val context = incomingContexts[key]!!
      return viewFactories.createView(context, key).also {
        warnIfIdIsMissing(it)
        maybeSetThemeBackground(it)
        incomingState.restore(it) // todo: check if Flow can save multiple Views here.
        hostView().addView(it)
      }
    }

    val oldForegroundView = hostView().children.lastOrNull()
    val newBackgroundView = incomingKey.background?.let(::findOrCreateView)
    val foregroundView = incomingKey.foreground.let(::findOrCreateView)
    foregroundView.bringToFront()

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

    val forwardTransition = wasStateRestored || oldForegroundView === newBackgroundView
    val fromView: View? = if (wasStateRestored) newBackgroundView else oldForegroundView
    val fromKey: ScreenKey? = if (wasStateRestored) incomingKey.background else outgoingKey?.foreground

    if (fromView != null) {
      transitions.first {
        it.transition(
          fromView = fromView,
          fromKey = fromKey!!,
          toView = foregroundView,
          toKey = incomingKey.foreground,
          goingForward = forwardTransition,
          onComplete = onTransitionEnd
        ) == Handled
      }
    } else {
      onTransitionEnd()
    }

    callback.onTraversalCompleted()
  }

  private fun peek(): CompositeScreenKey? {
    val children = hostView().children.toList()
      .asReversed()
      .ifEmpty { null } ?: return null

    return CompositeScreenKey(
      background = children.getOrNull(1)?.screenKey(),
      foreground = children[0].screenKey()
    )
  }

  private fun warnIfIdIsMissing(incomingView: View) {
    check(incomingView.id != View.NO_ID) {
      "${incomingView::class.simpleName} needs an ID for persisting View state."
    }
  }

  private fun maybeSetThemeBackground(view: View) {
    if (view.background == null) {
      view.themeAware {
        view.setBackgroundColor(it.window.backgroundColor)
      }
    }
  }

  private fun dispatchFocusChangeCallback() {
    val children = hostView().children.toList()
    val foregroundView = children.lastOrNull()

    children
      .filterIsInstance<ScreenFocusChangeListener>()
      .plus(focusChangeListeners)
      .forEach {
        it.onScreenFocusChanged(focusedScreen = foregroundView)
      }
  }

  fun onInterceptBackPress(): BackPressInterceptor.InterceptResult {
    val foreground = hostView().children.lastOrNull() as? BackPressInterceptor ?: return Ignored
    return foreground.onInterceptBackPress()
  }
}

private class NoOpTransition : ScreenTransition {
  override fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    goingForward: Boolean,
    onComplete: () -> Unit
  ): TransitionResult {
    onComplete()
    return Handled
  }
}
