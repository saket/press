package press.editor

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.animation.Interpolator
import android.widget.OverScroller
import androidx.core.widget.NestedScrollView
import press.extensions.reflect

// todo: rename to EditorScrollView.
class FadingEdgeNestedScrollView(context: Context) : NestedScrollView(context) {
  private var scrollDuration = 300  // NestedScrollView uses 250.

  init {
    isVerticalFadingEdgeEnabled = true
  }

  override fun getTopFadingEdgeStrength() = 0f    // Always hidden.
  override fun getBottomFadingEdgeStrength() = 1f // Always visible.

  fun setScrollAnimationProperties(duration: Long, interpolator: Interpolator) {
    scrollDuration = duration.toInt()

    reflect<NestedScrollView>().field("mScroller")?.let { field ->
      val scroller = field.get(this) as OverScroller
      val setter = reflect<OverScroller>().method("setInterpolator", Interpolator::class.java)
      setter?.invoke(scroller, interpolator)
    }
  }

  override fun requestChildRectangleOnScreen(child: View, rectangle: Rect, immediate: Boolean): Boolean {
    if (immediate) {
      return super.requestChildRectangleOnScreen(child, rectangle, immediate)
    }

    // Copied from NestedScrollView. Copying this code would have not
    // been needed if NestedScrollView#smoothScrollBy wasn't final.
    // https://issuetracker.google.com/issues/183218283
    rectangle.offset(child.left - child.scrollX, child.top - child.scrollY)
    val delta = computeScrollDeltaToGetChildRectOnScreen(rectangle)
    return (delta != 0).also {
      smoothScrollBy(0, delta, scrollDuration)
    }
  }
}
