package press.navigation

import android.content.Context
import android.view.MotionEvent
import android.widget.FrameLayout

class NavigationHostLayout(context: Context) : FrameLayout(context) {
  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    // Don't leak touch events to background screens, which
    // can happen if the foreground View doesn't consume an event.
    return getChildAt(childCount - 1)?.dispatchTouchEvent(ev) ?: super.dispatchTouchEvent(ev)
  }
}
