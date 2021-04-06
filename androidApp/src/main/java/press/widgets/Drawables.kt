package press.widgets

import android.content.res.ColorStateList
import android.graphics.Color.BLACK
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.RECTANGLE
import android.graphics.drawable.RippleDrawable
import me.saket.press.shared.theme.ThemePalette
import me.saket.press.shared.theme.withAlpha

object Drawables {
  fun roundedRect(color: Int, cornerRadius: Float): GradientDrawable {
    return GradientDrawable().also {
      it.shape = RECTANGLE
      it.cornerRadius = cornerRadius
      it.setColor(color)
    }
  }
}

fun GradientDrawable.withRipple(palette: ThemePalette): RippleDrawable {
  return this.withRipple(palette.pressedColor(this.color!!.defaultColor), rippleAlpha = 1f)
}

fun GradientDrawable.withRipple(rippleColor: Int, rippleAlpha: Float = 0.15f): RippleDrawable {
  val content = this
  val mask = GradientDrawable().also {
    it.cornerRadius = content.cornerRadius
    it.shape = content.shape
    it.setColor(BLACK)
  }

  check(rippleAlpha in 0f..1f)
  return RippleDrawable(ColorStateList.valueOf(rippleColor.withAlpha(rippleAlpha)), content, mask)
}
