package press.navigation

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import flow.Flow
import flow.KeyChanger
import flow.State
import flow.TraversalCallback
import me.saket.press.shared.ui.ScreenKey
import press.navigation.BackPressInterceptor.InterceptResult.Ignored

/**
 * Like a ViewFlipper for navigating between [ScreenKey]s.
 */
class ScreenKeyChanger(
  private val container: () -> ViewGroup,
  private val viewFactories: ViewFactories
) : KeyChanger {
  override fun changeKey(
    outgoingState: State?,
    incomingState: State,
    direction: flow.Direction,
    incomingContexts: Map<Any, Context>,
    callback: TraversalCallback
  ) {
    val incomingKey = incomingState.getKey<ScreenKey>()
    val container = container()

    if (outgoingState == null && direction == flow.Direction.REPLACE) {
      // Short circuit if we would just be showing the same view again. Flow
      // intentionally calls changeKey() again on onResume() with the same values.
      // See: https://github.com/square/flow/issues/173.
      if (isScreenAlreadyActive(container.getChildAt(0), incomingKey)) {
        callback.onTraversalCompleted()
        return
      }
    }

    val outgoingView: View? = container.getChildAt(0)
    val incomingContext = incomingContexts[incomingKey]!!
    val incomingView = createIncomingView(incomingContext, incomingKey)
    warnIfIdIsMissing(incomingView)

    incomingState.restore(incomingView)
    container.addView(incomingView)

    outgoingView?.let {
      outgoingState?.save(outgoingView)
      container.removeView(outgoingView)
    }

    callback.onTraversalCompleted()
  }

  private fun createIncomingView(incomingContext: Context, incomingKey: ScreenKey): View {
    return viewFactories.createView(incomingContext, incomingKey)
  }

  private fun warnIfIdIsMissing(incomingView: View) {
    check(incomingView.id != View.NO_ID) {
      "Screen's layout (${incomingView::class.simpleName}) doesn't have an ID set on its root ViewGroup. " +
        "An ID is required for persisting View state."
    }
  }

  private fun isScreenAlreadyActive(view: View?, initialKey: Any?): Boolean {
    return view?.let {
      Flow.getKey<Any?>(view) == initialKey
    } ?: false
  }

  fun onInterceptBackPress(): BackPressInterceptor.InterceptResult {
    val foreground = container().children.last() as? BackPressInterceptor ?: return Ignored
    return foreground.onInterceptBackPress()
  }
}
