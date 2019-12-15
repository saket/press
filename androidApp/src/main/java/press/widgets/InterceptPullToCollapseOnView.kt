package press.widgets

import android.view.View
import me.saket.inboxrecyclerview.page.InterceptResult
import me.saket.inboxrecyclerview.page.InterceptResult.IGNORED
import me.saket.inboxrecyclerview.page.InterceptResult.INTERCEPTED
import me.saket.inboxrecyclerview.page.OnPullToCollapseInterceptor

fun interceptPullToCollapseOnView(view: View): OnPullToCollapseInterceptor {
  return { downX, downY, upwardPull ->
    val touchLiesOnView = view.locationOnScreen().contains(downX.toInt(), downY.toInt())

    if (touchLiesOnView) {
      val directionInt = if (upwardPull) +1 else -1
      val canScrollFurther = view.canScrollVertically(directionInt)
      if (canScrollFurther) INTERCEPTED else IGNORED
    } else {
      IGNORED
    }
  }
}
