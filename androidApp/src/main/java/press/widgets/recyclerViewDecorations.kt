package press.widgets

import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import press.theme.themePalette

@Suppress("FunctionName")
fun View.DividerItemDecoration(orientation: Int = VERTICAL): RecyclerView.ItemDecoration {
  return DividerItemDecoration(context, orientation).apply {
    setDrawable(DividerDrawable(themePalette().divider))
  }
}

@Suppress("FunctionName")
fun View.SpacingItemDecoration(spacing: Int, orientation: Int): RecyclerView.ItemDecoration {
  return DividerItemDecoration(context, orientation).apply {
    setDrawable(object : DividerDrawable(Color.TRANSPARENT) {
      override fun getIntrinsicWidth(): Int = spacing
      override fun getIntrinsicHeight(): Int = spacing
    })
  }
}

// todo: use 1dp instead of 1px.
open class DividerDrawable(@ColorInt color: Int = themePalette().divider) : PaintDrawable(color) {
  override fun getIntrinsicWidth() = 1
  override fun getIntrinsicHeight() = 1
}
