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
import press.theme.themeAware
import java.util.Stack

/** Like a ViewFlipper for navigating between [ScreenKey]s. */
class ScreenKeyChanger(
  private val container: () -> ViewGroup,
  private val viewFactories: ViewFactories
) : KeyChanger {
  private val lruViewStack = LruViewStack(maxSize = 2)

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

    lruViewStack.push(incomingKey, context = incomingContexts[incomingKey]!!, incomingState)
    callback.onTraversalCompleted()
  }

  private inner class LruViewStack(val maxSize: Int) {
    private val stack = Stack<ViewEntry>()

    fun peek(): ScreenKey? {
      return if (stack.isEmpty()) null
      else stack.peek()?.key
    }

    fun push(screenKey: ScreenKey, context: Context, state: State) {
      // When navigating to a screen that's already
      // on the stack, reset the stack to the screen.
      if (stack.any { it.key == screenKey }) {
        while (stack.peek().key != screenKey) {
          saveAndRemoveView(stack.pop())
        }
        return
      }

      val view = viewFactories.createView<View>(context, screenKey).also {
        warnIfIdIsMissing(it)
        maybeSetThemeBackground(it)
      }

      state.restore(view)
      container().addView(view)
      stack.push(ViewEntry(state, view, screenKey))

      if (stack.size > maxSize) {
        saveAndRemoveView(stack.removeAt(0))
      }
    }

    private fun saveAndRemoveView(entry: ViewEntry) {
      entry.state?.save(entry.view)
      container().removeView(entry.view)
    }

    private fun warnIfIdIsMissing(incomingView: View) {
      check(incomingView.id != View.NO_ID) {
        "Screen's layout (${incomingView::class.simpleName}) doesn't have an ID set on its root ViewGroup. " +
          "An ID is required for persisting View state."
      }
    }

    private fun maybeSetThemeBackground(view: View) {
      if (view.background == null) {
        view.themeAware {
          view.setBackgroundColor(it.window.backgroundColor)
        }
      }
    }
  }

  class ViewEntry(
    val state: State?,
    val view: View,
    val key: ScreenKey
  )

  fun onInterceptBackPress(): BackPressInterceptor.InterceptResult {
    val foreground = container().children.last() as? BackPressInterceptor ?: return Ignored
    return foreground.onInterceptBackPress()
  }
}
