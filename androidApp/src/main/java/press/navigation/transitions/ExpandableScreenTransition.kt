package press.navigation.transitions

import android.graphics.Color.BLACK
import android.view.View
import androidx.appcompat.widget.Toolbar
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.animation.ItemExpandAnimator
import me.saket.inboxrecyclerview.dimming.DimPainter
import me.saket.inboxrecyclerview.expander.InboxItemExpander
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.press.shared.ui.ScreenKey
import press.extensions.doOnCollapse
import press.extensions.doOnExpand
import press.extensions.findChild
import press.navigation.ScreenTransition
import press.navigation.ScreenTransition.TransitionResult
import press.navigation.ScreenTransition.TransitionResult.Handled
import press.navigation.ScreenTransition.TransitionResult.Ignored

/**
 * Implemented by screens that support expansion of
 * incoming screens from their [InboxRecyclerView] list.
 */
interface ExpandableScreenHost {
  fun createScreenExpander(): InboxItemExpander<ScreenKey>
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
    newBackground: View?,
    goingForward: Boolean,
    onComplete: () -> Unit
  ): TransitionResult {
    val expandableHost = (if (goingForward) fromView else toView).findChild<ExpandableScreenHost>()
    val itemExpander = expandableHost?.createScreenExpander()

    if (goingForward && toView is ExpandablePageLayout && itemExpander != null) {
      val fromList = fromView.findChild<InboxRecyclerView>()!!
      fromList.attachPage(toView, itemExpander, parent = fromView)
      itemExpander.expandItem(toKey, immediate = !fromView.isLaidOut)
      toView.doOnExpand(onComplete)
      return Handled

    } else if (!goingForward && fromView is ExpandablePageLayout && itemExpander != null) {
      val toList = toView.findChild<InboxRecyclerView>()!!
      toList.attachPage(fromView, itemExpander, parent = toView)

      // This screen may have expanded from a list item that is no longer visible
      // because the keyboard caused the list to resize. Hide the keyboard before
      // collapsing so that the list item's View is added back.
      toList.hideKeyboardAndRun {
        itemExpander.setItem(fromKey)
        itemExpander.collapse()
      }
      fromView.doOnCollapse {
        toList.detachPage(fromView)
        onComplete()
      }
      return Handled
    }

    return Ignored
  }

  override fun prepareBackground(background: View, foreground: View, foregroundKey: ScreenKey) {
    // Background screens are expanded immediately on creation. They must be
    // wired with the expanded item manually for pull-to-collapse to work.
    if (foreground is ExpandablePageLayout) {
      background.findChild<ExpandableScreenHost>()?.let { bgHost ->
        val bgList = (bgHost as View).findChild<InboxRecyclerView>()!!
        val itemExpander = bgHost.createScreenExpander()
        bgList.attachPage(foreground, itemExpander, background)
        itemExpander.setItem(foregroundKey)
      }
    }
  }

  private fun InboxRecyclerView.attachPage(
    page: ExpandablePageLayout,
    itemExpander: InboxItemExpander<ScreenKey>,
    parent: View
  ) {
    this.itemExpander = itemExpander
    this.expandablePage = page
    this.itemExpandAnimator = ItemExpandAnimator.split()
    this.dimPainter = DimPainter.listAndPage(color = BLACK, alpha = 0.25f)
    page.pushParentToolbarOnExpand(toolbar = parent.findChild<Toolbar>()!!)
  }

  private fun InboxRecyclerView.detachPage(page: ExpandablePageLayout) {
    this.expandablePage = null
    page.pushParentToolbarOnExpand(null)
  }
}
