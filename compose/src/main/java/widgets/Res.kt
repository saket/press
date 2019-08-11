package widgets

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes

sealed class Res

class Attr(
  @AttrRes private val resId: Int,
  private val context: Context,
  private val typedValue: TypedValue = TypedValue()
): Res() {

  fun asDimension(): Int {
    context.theme.resolveAttribute(resId, typedValue, true)
    return TypedValue.complexToDimensionPixelSize(typedValue.data, context.resources.displayMetrics)
  }
}