package press.widgets.popup

import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.ScaleDrawable
import android.view.Gravity

internal class DrawSkippableDrawable(val delegate: Drawable) : DrawableWrapperCompat(delegate) {
  var skip = false

  override fun draw(canvas: Canvas) {
    if (!skip) {
      super.draw(canvas)
    }
  }
}

/** Because [DrawableWrapper] is API 23+ only. */
internal abstract class DrawableWrapperCompat(
  private val delegate: Drawable
) : ScaleDrawable(delegate, Gravity.CENTER, -1f, -1f) {

  override fun draw(canvas: Canvas) {
    delegate.draw(canvas)
  }

  override fun getOutline(outline: Outline) {
    // ScaleDrawable doesn't delegate getOutline()
    // calls to the wrapped drawable on API 21.
    delegate.getOutline(outline)
  }
}
