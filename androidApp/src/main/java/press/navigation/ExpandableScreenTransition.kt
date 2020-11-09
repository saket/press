package press.navigation

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks
import me.saket.press.shared.ui.ScreenKey
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Handled
import press.navigation.ScreenTransition.TransitionResult.Ignored
import java.util.ArrayDeque

/** Wires an [InboxRecyclerView] with its expandable page across screens. */
class ExpandableScreenTransition : ScreenTransition {
  override fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    goingForward: Boolean,
    onComplete: () -> Unit
  ): TransitionResult {
    if (goingForward && toKey is ExpandableScreenKey<*>) {
      check(toView is ExpandablePageLayout)
      toView.pushParentToolbarOnExpand(fromView.findChild<Toolbar>())
      toView.onNextExpand {
        onComplete()
      }
      val fromList = fromView.findChild<InboxRecyclerView>()!!
      fromList.expandablePage = toView
      fromList.expandItem(toKey.expandingFromItemId)
      return Handled

    } else if (!goingForward && fromKey is ExpandableScreenKey<*>) {
      check(fromView is ExpandablePageLayout)
      val toList = toView.findChild<InboxRecyclerView>()!!
      fromView.onNextCollapse {
        toList.expandablePage = null
        fromView.pushParentToolbarOnExpand(null)
        onComplete()
      }
      toList.collapse()
      return Handled
    }

    return Ignored
  }
}

private inline fun <reified T> View.findChild(): T? {
  val queue = ArrayDeque<View>()
  queue.addFirst(this)

  while (true) {
    when (val current = queue.poll()) {
      null -> error("Couldn't find ${T::class.qualifiedName} in ${this::class.qualifiedName}'s hierarchy.")
      is T -> return current
      is ViewGroup -> current.forEach { queue.addLast(it) }
    }
  }
}

private inline fun ExpandablePageLayout.onNextExpand(
  crossinline block: (ExpandablePageLayout) -> Unit
) {
  val page = this
  addStateChangeCallbacks(object : SimplePageStateChangeCallbacks() {
    override fun onPageExpanded() {
      block(page)
      removeStateChangeCallbacks(this)
    }
  })
}

private inline fun ExpandablePageLayout.onNextCollapse(
  crossinline block: (ExpandablePageLayout) -> Unit
) {
  val page = this
  addStateChangeCallbacks(object : SimplePageStateChangeCallbacks() {
    override fun onPageCollapsed() {
      block(page)
      removeStateChangeCallbacks(this)
    }
  })
}
