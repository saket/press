package press.navigation.transitions

import android.view.View
import androidx.appcompat.widget.Toolbar
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.ui.ScreenKey
import press.extensions.findChild
import press.navigation.ScreenTransition
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Handled
import press.navigation.ScreenTransition.TransitionResult.Ignored

interface ExpandableScreenHost {
  fun itemIdForExpandingScreen(screen: ScreenKey): Long?
}

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
    if (fromKey is HomeScreenKey && toView is ExpandablePageLayout) {
      val fromList = fromView.findChild<InboxRecyclerView>()!!
      setupInboxList(list = fromList, listParent = fromView, page = toView)

      val expandingItemId = fromView.findChild<ExpandableScreenHost>()?.itemIdForExpandingScreen(toKey)
      if (expandingItemId != null) fromList.expandItem(expandingItemId, immediate = !fromView.isLaidOut)
      else fromList.expandFromTop(immediate = !fromView.isLaidOut)
      toView.doOnExpand {
        onComplete()
      }
      return Handled

    } else if (fromView is ExpandablePageLayout && toKey is HomeScreenKey) {
      val toList = toView.findChild<InboxRecyclerView>()!!
      setupInboxList(list = toList, listParent = toView, page = fromView)

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

private fun setupInboxList(list: InboxRecyclerView, listParent: View, page: ExpandablePageLayout) {
  page.pushParentToolbarOnExpand(toolbar = listParent.findChild<Toolbar>())
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
