package press.navigation.transitions

import android.view.View
import androidx.appcompat.widget.Toolbar
import me.saket.inboxrecyclerview.ExpandedItemFinder
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.animation.ItemExpandAnimator
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.StandaloneExpandablePageLayout
import me.saket.press.shared.ui.ScreenKey
import press.extensions.doOnCollapse
import press.extensions.doOnExpand
import press.extensions.findChild
import press.navigation.ScreenTransition
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Handled
import press.navigation.ScreenTransition.TransitionResult.Ignored
import press.navigation.screenKey

/**
 * Implemented by screens that support expansion of
 * incoming screens from their [InboxRecyclerView] list.
 */
interface ExpandableScreenHost {
  fun identifyExpandingItem(): ExpandedItemFinder?
}

/**
 * Wires an [InboxRecyclerView] with its expandable
 * page across screens for expand/collapse transition.
 */
class ExpandableScreenTransition : ScreenTransition {
  override fun prepareBackground(background: View, foreground: View, foregroundKey: ScreenKey) {
    // Background screens are expanded immediately on creation. They must be
    // wired with the expanded item manually for pull-to-collapse to work.
    if (background is ExpandableScreenHost && foreground is ExpandablePageLayout) {
      background.findChild<InboxRecyclerView>()?.let { bgList ->
        val bgHost = background.findChild<ExpandableScreenHost>()!!
        bgList.attachPage(foreground, bgHost, background)  // Will be detached on collapse during transition.
        bgList.forceUpdateExpandedItem(foregroundKey)
      }
    }
  }

  override fun transition(
    fromView: View,
    fromKey: ScreenKey,
    toView: View,
    toKey: ScreenKey,
    newBackground: View?,
    goingForward: Boolean,
    onComplete: () -> Unit
  ): TransitionResult {
    val expandableHost = (if (goingForward) fromView else toView).findChild<ExpandableScreenHost>()

    if (goingForward && toView is ExpandablePageLayout && expandableHost != null) {
      val fromList = fromView.findChild<InboxRecyclerView>()!!
      fromList.attachPage(toView, expandableHost, parent = fromView)
      fromList.expandItem(toKey, immediate = !fromView.isLaidOut)
      toView.doOnExpand(onComplete)
      return Handled

    } else if (!goingForward && fromView is ExpandablePageLayout && expandableHost != null) {
      val toList = toView.findChild<InboxRecyclerView>()!!
      toList.attachPage(fromView, expandableHost, parent = toView)

      toList.collapse()
      fromView.doOnCollapse {
        toList.detachPage(fromView)
        onComplete()
      }
      return Handled
    }

    return Ignored
  }

  private fun InboxRecyclerView.attachPage(
    page: ExpandablePageLayout,
    expandableHost: ExpandableScreenHost,
    parent: View
  ) {
    this.expandedItemFinder = expandableHost.identifyExpandingItem()
    this.expandablePage = page
    this.itemExpandAnimator = ItemExpandAnimator.scale()
    page.pushParentToolbarOnExpand(toolbar = parent.findChild<Toolbar>()!!)
  }

  private fun InboxRecyclerView.detachPage(page: ExpandablePageLayout) {
    this.expandablePage = null
    page.pushParentToolbarOnExpand(null)
  }
}
