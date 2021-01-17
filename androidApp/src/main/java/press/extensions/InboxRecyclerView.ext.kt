package press.extensions

import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks

inline fun ExpandablePageLayout.doOnExpand(crossinline action: () -> Unit) {
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

inline fun ExpandablePageLayout.doOnCollapse(crossinline block: () -> Unit) {
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
