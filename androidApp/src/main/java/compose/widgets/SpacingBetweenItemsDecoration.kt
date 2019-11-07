package compose.widgets

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacingBetweenItemsDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
  override fun getItemOffsets(rect: Rect, v: View, rv: RecyclerView, state: RecyclerView.State) {
    rect.bottom = spaceHeight
  }
}
