package press.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources

fun Context.getDrawable(@DrawableRes resId: Int, @ColorInt tint: Int): Drawable {
  val drawable = AppCompatResources.getDrawable(this, resId) ?: error("Can't load ${resources.getResourceName(resId)}")
  return drawable.mutate().apply { setTint(tint) }
}
