package press.editor

import android.content.Context
import androidx.core.widget.NestedScrollView

class FadingEdgeNestedScrollView(context: Context) : NestedScrollView(context) {
  init {
    isVerticalFadingEdgeEnabled = true
  }

  override fun getBottomFadingEdgeStrength(): Float {
    return 1f // Always visible.
  }

  override fun getTopFadingEdgeStrength(): Float {
    return 0f // Hidden.
  }
}
