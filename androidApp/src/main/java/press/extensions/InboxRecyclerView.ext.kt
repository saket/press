package press.extensions

import android.view.View
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.InterceptResult.IGNORED
import me.saket.inboxrecyclerview.page.InterceptResult.INTERCEPTED
import me.saket.inboxrecyclerview.page.OnPullToCollapseInterceptor
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks

fun interceptPullToCollapseOnView(scrollableView: View, toolbar: View? = null): OnPullToCollapseInterceptor {
  return { downX, downY, upwardPull ->
    if (toolbar?.locationOnScreen()?.contains(downX.toInt(), downY.toInt()) == true) {
      IGNORED
    } else {
      if (scrollableView.locationOnScreen().contains(downX.toInt(), downY.toInt())) {
        val directionInt = if (upwardPull) +1 else -1
        val canScrollFurther = scrollableView.canScrollVertically(directionInt)
        if (canScrollFurther) INTERCEPTED else IGNORED
      } else {
        IGNORED
      }
    }
  }
}

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
