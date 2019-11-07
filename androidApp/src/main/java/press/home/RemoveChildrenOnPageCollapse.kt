package press.home

import android.view.ViewGroup
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks

class RemoveChildrenOnPageCollapse(
  private val viewGroup: ViewGroup
) : SimplePageStateChangeCallbacks() {

  override fun onPageCollapsed() {
    viewGroup.removeAllViews()
  }
}
