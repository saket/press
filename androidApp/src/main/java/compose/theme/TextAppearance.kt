package compose.theme

import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.StyleRes
import compose.util.textColor

data class TextAppearance(
  @StyleRes val parentRes: Int? = null,
  val textSizeSp: Float = 15f,
  val letterSpacing: Float = 0f,
  val textColor: Int,
  val typeface: Typeface = Typeface.DEFAULT
) : Styleable<TextView> {

  override fun style(view: TextView) {
    if (parentRes != null) {
      view.setTextAppearance(parentRes)
    }

    view.textSize = textSizeSp
    view.letterSpacing = letterSpacing
    view.textColor = textColor
    view.typeface = typeface
  }
}
