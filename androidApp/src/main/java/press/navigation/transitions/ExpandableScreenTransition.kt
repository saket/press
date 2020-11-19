package press.navigation.transitions

import android.view.View
import androidx.appcompat.widget.Toolbar
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.animation.ItemExpandAnimator
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks
import me.saket.press.shared.editor.EditorOpenMode.ExistingNote
import me.saket.press.shared.editor.EditorScreenKey
import me.saket.press.shared.home.HomeScreenKey
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
    onComplete: () -> Unit
  ): TransitionResult {
    if (fromKey is HomeScreenKey && toView is ExpandablePageLayout) {
      val fromList = fromView.findChild<InboxRecyclerView>()!!
      fromList.attachPage(toView, parent = fromView)

      when (val id = itemIdForExpandingScreen(toKey)) {
        null -> fromList.expandFromTop(immediate = !fromView.isLaidOut)
        else -> fromList.expandItem(id, immediate = !fromView.isLaidOut)
      }
      toView.doOnExpand {
        onComplete()
      }
      return Handled

    } else if (fromView is ExpandablePageLayout && toKey is HomeScreenKey) {
      val toList = toView.findChild<InboxRecyclerView>()!!
      toList.attachPage(fromView, parent = toView)

      toList.collapse()
      fromView.doOnCollapse {
        toList.detachPage(fromView)
        onComplete()
      }
      return Handled
    }

    return Ignored
  }

  private fun InboxRecyclerView.attachPage(page: ExpandablePageLayout, parent: View) {
    this.expandablePage = page
    this.itemExpandAnimator = ItemExpandAnimator.scale()
    page.pushParentToolbarOnExpand(toolbar = parent.findChild<Toolbar>()!!)
  }

  private fun InboxRecyclerView.detachPage(page: ExpandablePageLayout) {
    this.expandablePage = null
    page.pushParentToolbarOnExpand(null)
  }

  private fun itemIdForExpandingScreen(screen: ScreenKey): Long? {
    return if (screen is EditorScreenKey && screen.openMode is ExistingNote) {
      (screen.openMode as ExistingNote).listAdapterId
    } else {
      null
    }
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
}
