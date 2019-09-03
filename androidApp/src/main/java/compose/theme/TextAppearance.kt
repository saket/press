package compose.theme

import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.StyleRes
import compose.util.textColor

data class TextAppearance(
  @StyleRes val parentRes: Int? = null,
  val textSizeSp: Float? = null,
  val letterSpacing: Float? = null,
  val textColor: Int,
  val typeface: Typeface? = null
) : Styleable<TextView> {

  override fun style(view: TextView) {
    parentRes?.let(view::setTextAppearance)
    textSizeSp?.let(view::setTextSize)
    letterSpacing?.let(view::setLetterSpacing)
    typeface?.let(view::setTypeface)

    view.textColor = textColor
  }
}
