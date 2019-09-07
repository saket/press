package compose.widgets

import android.graphics.drawable.Drawable
import me.saket.resourceinterceptor.DrawableInterceptor

class TintedCursorDrawable(private val color: Int) : DrawableInterceptor {
  override fun intercept(systemDrawable: () -> Drawable?): Drawable {
    val cursor = systemDrawable()!!
    return cursor.mutate().apply { setTint(color) }
  }
}