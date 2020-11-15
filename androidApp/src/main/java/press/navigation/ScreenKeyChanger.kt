package press.navigation

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import flow.KeyChanger
import flow.State
import flow.TraversalCallback
import me.saket.press.shared.ui.ScreenKey
import press.navigation.BackPressInterceptor.InterceptResult.Ignored
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Handled
import press.theme.themeAware
import java.util.Stack

/** Inflates screen Views in response to backstack changes. */
class ScreenKeyChanger(
  private val hostView: () -> ViewGroup,
  private val viewFactories: ViewFactories,
  transitions: List<ScreenTransition>
) : KeyChanger {
  private val lruViewStack = LruViewStack(maxSize = 2)
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
    if (outgoingState == null && direction == flow.Direction.REPLACE) {
      // Short circuit if we would just be showing the same view again. Flow
      // intentionally calls changeKey() again on onResume() with the same values.
      // See: https://github.com/square/flow/issues/173.
      if (lruViewStack.peek() == incomingKey) {
        callback.onTraversalCompleted()
        return
      }
    }

    if (incomingKey !is PlaceholderScreenKey) {
      lruViewStack.push(incomingKey, context = incomingContexts[incomingKey]!!, incomingState)
    }
    callback.onTraversalCompleted()
  }

  /**
   * Keeps multiple screen Views stacked on top of each other so that they can interact
   * together. For screens of type [ExpandableScreenKey], pulling a foreground screen will
   * reveal its background screen.
   */
  private inner class LruViewStack(val maxSize: Int) {
    private val stack = Stack<ViewEntry>()

    fun peek(): ScreenKey? {
      return if (stack.isEmpty()) null
      else stack.peek()?.key
    }

    fun push(screenKey: ScreenKey, context: Context, state: State) {
      // When navigating to a screen that's already
      // on the stack, treat this as going back.
      if (stack.any { it.key == screenKey }) {
        popUntil(screenKey)
        return
      }

      val foreground = viewFactories.createView(context, screenKey).also {
        warnIfIdIsMissing(it)
        maybeSetThemeBackground(it)
      }

      state.restore(foreground)
      hostView().addView(foreground)
      dispatchFocusChangeCallback()

      stack.push(ViewEntry(state, foreground, screenKey))

      if (stack.size >= 2) {
        val background = stack[stack.size - 2]
        transitions.first {
          it.transition(
            fromView = background.view,
            fromKey = background.key,
            toView = foreground,
            toKey = screenKey,
            goingForward = true
          ) == Handled
        }
      }

      if (stack.size > maxSize) {
        val stale = stack.removeAt(0)
        saveAndRemoveView(stale)
      }
    }

    private fun popUntil(key: ScreenKey) {
      if (stack.peek().key == key) {
        return
      }
      val popped = stack.pop()
      transitions.first {
        it.transition(
          fromView = popped.view,
          fromKey = popped.key,
          toView = stack.peek().view,
          toKey = stack.peek().key,
          goingForward = false,
          onComplete = {
            saveAndRemoveView(popped)
            popUntil(key)
          }
        ) == Handled
      }
    }

    private fun saveAndRemoveView(entry: ViewEntry) {
      entry.state?.save(entry.view)
      hostView().removeView(entry.view)
      dispatchFocusChangeCallback()
    }

    private fun warnIfIdIsMissing(incomingView: View) {
      check(incomingView.id != View.NO_ID) {
        "${incomingView::class.simpleName} needs an ID for persisting View state."
      }
    }
  }

  private class ViewEntry(
    val state: State?,
    val view: View,
    val key: ScreenKey
  )

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
      .forEach {
        it.onScreenFocusChanged(focusedScreen = foregroundView)
      }
    focusChangeListeners.forEach {
      it.onScreenFocusChanged(foregroundView)
    }
  }

  fun onInterceptBackPress(): BackPressInterceptor.InterceptResult {
    val foreground = hostView().children.last() as? BackPressInterceptor ?: return Ignored
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
