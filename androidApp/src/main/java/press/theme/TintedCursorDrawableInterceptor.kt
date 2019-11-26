package press.theme

import android.graphics.PorterDuff.Mode.SRC_ATOP
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import me.saket.resourceinterceptor.DrawableInterceptor
import me.saket.resourceinterceptor.SystemDrawable

class TintedCursorDrawableInterceptor(
  private val activity: AppCompatActivity
) : DrawableInterceptor {

  override fun intercept(systemDrawable: SystemDrawable): Drawable? {
    return systemDrawable().apply {
      activity.themeAware { palette ->
        setColorFilter(palette.accentColor, SRC_ATOP)
        invalidateSelf()
      }
    }
  }
}
