package press.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color.BLACK
import android.graphics.Color.GRAY
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.setPadding
import me.saket.press.shared.theme.UiStyles
import me.saket.press.shared.theme.applyStyle
import press.extensions.updatePadding
import press.theme.themeAware

open class PressButton(context: Context, style: UiStyles.Text) : AppCompatButton(context) {
  init {
    minHeight = 0
    minimumHeight = 0
    minWidth = 0
    minimumWidth = 0
    isAllCaps = false
    applyStyle(style)
    updatePadding(horizontal = dp(16), vertical = dp(8))
    themeAware {
      background = pressButtonDrawable(it.buttonNormal, pressedColor = it.buttonPressed)
    }
  }

  override fun setTextColor(color: Int) {
    super.setTextColor(
      colorStateListOf(
        intArrayOf(-android.R.attr.state_enabled) to GRAY,
        intArrayOf() to color
      )
    )
  }
}

class PressBorderlessButton(context: Context, style: UiStyles.Text) : PressButton(context, style) {
  init {
    elevation = 0f
    stateListAnimator = null
    themeAware {
      background = pressButtonDrawable(TRANSPARENT, pressedColor = it.buttonPressed, rounded = false)
    }
  }
}

class PressBorderlessImageButton(context: Context) : AppCompatImageButton(context) {
  init {
    elevation = 0f
    stateListAnimator = null
    setPadding(dp(16))
    themeAware {
      background = RippleDrawable(ColorStateList.valueOf(it.buttonPressed), null, null)
    }
  }
}

@Suppress("FunctionName")
fun View.pressButtonDrawable(color: Int, pressedColor: Int, rounded: Boolean = true): Drawable {
  val rippleColor = ColorStateList.valueOf(pressedColor)
  val shape = PaintDrawable(color).apply {
    if (rounded) {
      setCornerRadius(dp(20f))
    }
  }
  val mask = PaintDrawable(BLACK).apply {
    if (rounded) {
      setCornerRadius(dp(20f))
    }
  }
  return RippleDrawable(rippleColor, shape, mask)
}
