package press.navigation.transitions

import android.view.View
import kotlinx.android.parcel.Parcelize
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.SimpleOnPullListener
import me.saket.press.shared.ui.ScreenKey
import press.navigation.DelegatingScreenKey
import press.navigation.navigator

/**
 * Makes [screen] expandable by wrapping it inside an [ExpandablePageLayout] and expands/collapses
 * from/to a [InboxRecyclerView] list item through [ExpandableScreenTransition].
 */
@Parcelize
data class ExpandableScreenKey(
  val screen: ScreenKey,
  val expandingFromItemId: Long? = null // When null, the screen will expand from the top.
) : DelegatingScreenKey(screen) {

  override fun transformDelegateView(view: View): View {
    return ExpandablePageLayout(view.context).apply {
      check(view.id != View.NO_ID)
      addView(view)
      id = view.id
      view.id = View.NO_ID

      applyStyle()
      onNextPullToCollapse {
        navigator().goBack()
      }
    }
  }
}

private inline fun ExpandablePageLayout.onNextPullToCollapse(
  crossinline block: (ExpandablePageLayout) -> Unit
) {
  val page = this
  addOnPullListener(object : SimpleOnPullListener() {
    override fun onRelease(collapseEligible: Boolean) {
      if (collapseEligible) {
        block(page)
        removeOnPullListener(this)
      }
    }
  })
}
