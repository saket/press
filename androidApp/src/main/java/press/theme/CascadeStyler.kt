package press.theme

import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.view.View
import me.saket.cascade.CascadePopupMenu
import me.saket.press.shared.theme.TextStyles
import me.saket.press.shared.theme.ThemePalette
import me.saket.press.shared.theme.applyStyle
import me.saket.wysiwyg.style.withOpacity
import press.extensions.rippleDrawable
import press.extensions.textColor
import press.widgets.dp

fun View.pressCascadeStyler(palette: ThemePalette): CascadePopupMenu.Styler {
  return CascadePopupMenu.Styler(
    background = {
      PaintDrawable(palette.buttonNormal, radius = dp(4f))
    },
    menuList = {
      AutoThemer.themeGroup(it)
    },
    menuTitle = {
      it.titleView.textColor = palette.textColorPrimary.withOpacity(0.5f)
      it.titleView.applyStyle(TextStyles.smallTitle)
      it.itemView.background = rippleDrawable(palette)
    },
    menuItem = {
      it.titleView.textColor = palette.textColorPrimary
      it.titleView.applyStyle(TextStyles.smallBody)
      it.itemView.background = rippleDrawable(palette)
    }
  )
}

@Suppress("FunctionName")
private fun PaintDrawable(color: Int, radius: Float): Drawable {
  return PaintDrawable(color).also { it.setCornerRadius(radius) }
}
