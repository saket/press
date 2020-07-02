package press.theme

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import press.widgets.dp

@Suppress("FunctionName")
fun View.PressButtonDrawable(color: Int, pressedColor: Int): Drawable {
  val rippleColor = ColorStateList.valueOf(pressedColor)
  val shape = PaintDrawable(color).apply {
    setCornerRadius(dp(20f))
  }
  val mask = PaintDrawable(Color.BLACK).apply {
    setCornerRadius(dp(20f))
  }
  return RippleDrawable(rippleColor, shape, mask)
}
