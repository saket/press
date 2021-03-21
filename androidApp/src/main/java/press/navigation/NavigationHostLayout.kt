package press.navigation

import android.app.Activity
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import press.widgets.insets.KeyboardInsetsChangeAnimator

class NavigationHostLayout(activity: Activity) : FrameLayout(activity) {
  init {
    // Press is going to handle insets on its own from this point.
    WindowCompat.setDecorFitsSystemWindows(activity.window, false)

    val insetAnimator = KeyboardInsetsChangeAnimator(this)
    ViewCompat.setWindowInsetsAnimationCallback(this, insetAnimator)
    ViewCompat.setOnApplyWindowInsetsListener(this, insetAnimator)
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    // Don't leak touch events to background screens, which
    // can happen if the foreground View doesn't consume an event.
    getChildAt(childCount - 1)?.let { foreground ->
      ev.offsetLocation(-foreground.left.toFloat(), -foreground.top.toFloat())
      return foreground.dispatchTouchEvent(ev)
    }
    return super.dispatchTouchEvent(ev)
  }
}
