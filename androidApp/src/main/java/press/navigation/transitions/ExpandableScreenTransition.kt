package press.navigation.transitions

import android.view.View
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.animation.ItemExpandAnimator
import me.saket.inboxrecyclerview.dimming.AnimatedVisibilityColorDrawable
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
  /** A View that will be pushed up by the expanding screen. */
  val toolbar: View

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
      fromList.attachPage(toView, itemExpander, hostToolbar = expandableHost.toolbar, parent = fromView)
      itemExpander.expandItem(toKey, immediate = !fromView.isLaidOut)
      toView.doOnExpand(onComplete)
      return Handled

    } else if (!goingForward && fromView is ExpandablePageLayout && itemExpander != null) {
      val toList = toView.findChild<InboxRecyclerView>()!!
      toList.attachPage(fromView, itemExpander, hostToolbar = expandableHost.toolbar, parent = toView)

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
        bgList.attachPage(foreground, itemExpander, hostToolbar = bgHost.toolbar, parent = background)
        itemExpander.setItem(foregroundKey)
      }
    }
  }

  private fun InboxRecyclerView.attachPage(
    page: ExpandablePageLayout,
    itemExpander: InboxItemExpander<ScreenKey>,
    hostToolbar: View?,
    parent: View
  ) {
    this.itemExpander = itemExpander
    this.expandablePage = page
    this.itemExpandAnimator = ItemExpandAnimator.split()
    this.dimPainter = ExpandableScreenDimPainter(
      // These foregrounds are set in PhoneFormFactor.
      listDrawable = parent.foreground as AnimatedVisibilityColorDrawable,
      pageDrawable = page.foreground as AnimatedVisibilityColorDrawable
    )
    page.pushParentToolbarOnExpand(hostToolbar)
  }

  private fun InboxRecyclerView.detachPage(page: ExpandablePageLayout) {
    this.expandablePage = null
    this.dimPainter = DimPainter.none()
    page.pushParentToolbarOnExpand(null)
  }
}
