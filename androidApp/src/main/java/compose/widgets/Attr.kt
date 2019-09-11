package compose.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.content.res.AppCompatResources

class Attr(
  @AttrRes private val resId: Int,
  private val context: Context,
  private val typedValue: TypedValue = TypedValue()
) {

  fun asDimension(): Int {
    resolveAttribute()
    return TypedValue.complexToDimensionPixelSize(typedValue.data, context.resources.displayMetrics)
  }

  fun asDrawable(): Drawable? {
    resolveAttribute()
    return AppCompatResources.getDrawable(context, typedValue.resourceId)
  }

  private fun resolveAttribute() {
    context.theme.resolveAttribute(resId, typedValue, true)
  }
}
