package press.theme

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.view.View
import me.saket.cascade.CascadePopupMenu
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextStyles.smallTitle
import me.saket.press.shared.theme.applyStyle
import me.saket.wysiwyg.style.withOpacity
import press.extensions.rippleDrawable
import press.extensions.textColor
import press.widgets.dp

fun View.pressCascadeStyler(): CascadePopupMenu.Styler {
  val palette = themePalette()
  return CascadePopupMenu.Styler(
    background = {
      PaintDrawable(palette.buttonNormal, radius = dp(4f))
    },
    menuList = {
      AutoThemer.themeGroup(it)
    },
    menuTitle = {
      it.titleView.applyStyle(smallTitle)
      it.titleView.textColor = palette.textColorPrimary.withOpacity(0.5f)
      it.itemView.background = rippleDrawable(palette)
    },
    menuItem = {
      it.titleView.applyStyle(smallBody)
      it.titleView.textColor = palette.textColorPrimary
      it.iconView.imageTintList = ColorStateList.valueOf(palette.accentColor)
      it.itemView.background = rippleDrawable(palette)
    }
  )
}

@Suppress("FunctionName")
private fun PaintDrawable(color: Int, radius: Float): Drawable {
  return PaintDrawable(color).also { it.setCornerRadius(radius) }
}
