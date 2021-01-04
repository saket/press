package press.navigation

import android.content.Context
import flow.Direction
import flow.KeyChanger
import flow.State
import flow.TraversalCallback
import java.util.Stack

abstract class EnqueuingKeyChanger : KeyChanger, TransitionCallback {
  private val queue = Stack<() -> Unit>()

  override fun changeKey(
    outgoingState: State?,
    incomingState: State,
    direction: Direction,
    incomingContexts: Map<Any, Context>,
    callback: TraversalCallback
  ) {
    queue.push {
      changeKey(outgoingState, incomingState, direction, incomingContexts, callback, transitionCallback = this)
    }

    if (queue.size == 1) {
      queue.peek().invoke()
    }
  }

  override fun onTransitionCompleted() {
    queue.removeFirst()

    if (queue.isNotEmpty()) {
      queue.peek().invoke()
    }
  }

  abstract fun changeKey(
    outgoingState: State?,
    incomingState: State,
    direction: Direction,
    incomingContexts: Map<Any, Context>,
    traversalCallback: TraversalCallback,
    transitionCallback: TransitionCallback
  )
}

interface TransitionCallback {
  fun onTransitionCompleted()
}
