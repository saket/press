package press.navigation.transitions

import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.dimming.AnimatedVisibilityColorDrawable
import me.saket.inboxrecyclerview.dimming.DimPainter
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState.COLLAPSED
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState.COLLAPSING
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState.EXPANDED
import me.saket.inboxrecyclerview.page.ExpandablePageLayout.PageState.EXPANDING

/**
 * Draws dimming over expandable screens. Unlike [DimPainter], this draws dimming over
 * the entire parent screen of an [InboxRecyclerView] instead of just the [InboxRecyclerView].
 */
open class ExpandableScreenDimPainter(
  private val listDrawable: AnimatedVisibilityColorDrawable,
  private val pageDrawable: AnimatedVisibilityColorDrawable,
) : DimPainter() {

  override fun onPageMove(rv: InboxRecyclerView, page: ExpandablePageLayout) {
    listDrawable.setShown(
      when (pageState(page)) {
        COLLAPSING, COLLAPSED -> false
        EXPANDING -> true
        EXPANDED -> !page.isCollapseEligible
      }
    )
    pageDrawable.setShown(
      when (pageState(page)) {
        COLLAPSING -> false
        COLLAPSED, EXPANDING -> false
        EXPANDED -> page.isCollapseEligible
      }
    )
  }

  open fun pageState(page: ExpandablePageLayout): PageState {
    return page.currentState
  }

  override fun cancelAnimation(
    rv: InboxRecyclerView,
    page: ExpandablePageLayout,
    resetDim: Boolean
  ) {
    listDrawable.cancelAnimation(setAlphaTo = if (resetDim) 0 else null)
    pageDrawable.cancelAnimation(setAlphaTo = if (resetDim) 0 else null)
  }
}
