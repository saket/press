package press.navigation.transitions

import android.view.View
import androidx.appcompat.widget.Toolbar
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks
import me.saket.press.shared.ui.ScreenKey
import press.extensions.findChild
import press.navigation.ScreenTransition
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Handled
import press.navigation.ScreenTransition.TransitionResult.Ignored

/**
 * Wires an [InboxRecyclerView] with its expandable
 * page across screens for expand/collapse transition.
 */
class ExpandableScreenTransition : ScreenTransition {
  override fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    goingForward: Boolean,
    onComplete: () -> Unit
  ): TransitionResult {
    if (goingForward && toKey is ExpandableScreenKey) {
      check(toView is ExpandablePageLayout)
      val fromList = fromView.findChild<InboxRecyclerView>()!!
      setupInboxList(list = fromList, page = toView, toolbar = fromView.findChild<Toolbar>())

      fromList.expandItem(toKey.expandingFromItemId, immediate = !fromView.isLaidOut)
      toView.doOnExpand {
        onComplete()
      }
      return Handled

    } else if (!goingForward && fromKey is ExpandableScreenKey) {
      check(fromView is ExpandablePageLayout)
      val toList = toView.findChild<InboxRecyclerView>()!!
      setupInboxList(list = toList, page = fromView, toolbar = toView.findChild<Toolbar>())

      toList.collapse()
      fromView.doOnCollapse {
        toList.expandablePage = null
        fromView.pushParentToolbarOnExpand(null)
        onComplete()
      }
      return Handled
    }

    return Ignored
  }
}

private fun setupInboxList(list: InboxRecyclerView, page: ExpandablePageLayout, toolbar: Toolbar?) {
  page.pushParentToolbarOnExpand(toolbar)
  list.expandablePage = page
}

private inline fun ExpandablePageLayout.doOnExpand(crossinline action: () -> Unit) {
  if (isExpanded) {
    action()
  } else {
    addStateChangeCallbacks(object : SimplePageStateChangeCallbacks() {
      override fun onPageExpanded() {
        action()
        removeStateChangeCallbacks(this)
      }
    })
  }
}

private inline fun ExpandablePageLayout.doOnCollapse(crossinline block: () -> Unit) {
  if (isCollapsed) {
    block()
  } else {
    addStateChangeCallbacks(object : SimplePageStateChangeCallbacks() {
      override fun onPageCollapsed() {
        block()
        removeStateChangeCallbacks(this)
      }
    })
  }
}
