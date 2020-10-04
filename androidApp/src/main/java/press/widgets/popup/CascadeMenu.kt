package press.widgets.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Color.WHITE
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.os.Build.VERSION.SDK_INT
import android.view.Gravity
import android.view.Gravity.CENTER_VERTICAL
import android.view.Gravity.START
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnMenuItemClickListener
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.iterator
import androidx.core.view.updatePaddingRelative
import me.saket.press.R
import press.extensions.getDrawable

/**
 * TODO
 *  - window margins.
 *  - use theme colors
 *    - menu background
 *    - item ripple color
 *    - item icon
 *    - submenu indicator triangle
 * */
@SuppressLint("RestrictedApi")
open class CascadeMenu(
  private val context: Context,
  private val styler: Styler,
  fixedWidthInDp: Int = 200
) : CascadePopupWindow(context) {
  val menu: Menu = MenuBuilder(context)
  var onMenuItemClickListener: OnMenuItemClickListener? = null
  private val fixedWidth = fixedWidthInDp.dip

  class Styler(
    val background: (Drawable) -> Drawable = { it },
    val menuTitle: (TextView, MenuItem) -> Unit = { _, _ -> },
    val menuItem: (TextView, MenuItem) -> Unit = { _, _ -> }
  )

  override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
    prepareMenuContent()
    super.showAsDropDown(anchor, xoff, yoff, gravity)
  }

  override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
    prepareMenuContent()
    super.showAtLocation(parent, gravity, x, y)
  }

  private fun prepareMenuContent() {
    contentView = HeightAnimatableViewFlipper(context).apply {
      background = styler.background(PaintDrawable(WHITE).apply { setCornerRadius(2f.dip) })
      clipToOutline = true
    }
    showMenu(menu)
  }

  private fun Menu.createLayout(): View {
    return LinearLayout(context).apply {
      orientation = VERTICAL
      layoutParams = LayoutParams(fixedWidth, WRAP_CONTENT)

      if (this@createLayout is SubMenu) {
        addView(TextView(context).also {
          it.textSize = 14f
          it.text = item.title
          it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
          it.updatePaddingRelative(start = 16.dip, end = 16.dip, top = 16.dip, bottom = 8.dip)
          styler.menuTitle(it, item)
        })
      }

      for (item in this@createLayout) {
        addView(TextView(context).also {
          it.textSize = 16f
          it.text = item.title
          it.background = createRippleDrawable(Color.GRAY)
          it.gravity = START or CENTER_VERTICAL
          it.compoundDrawablePadding = 14.dip

          val icon = item.tintedIcon()
          val subMenuIndicator = if (item.subMenu != null) createIndicatorIcon() else null
          it.setCompoundDrawables(start = icon, end = subMenuIndicator)
          it.updatePaddingRelative(
              start = if (item.icon != null) it.compoundDrawablePadding else 16.dip,
              end = if (subMenuIndicator != null) 4.dip else 16.dip,
              top = 16.dip,
              bottom = 16.dip
          )
          styler.menuItem(it, item)
          it.setOnClickListener { handleOnClick(item) }
        }, MATCH_PARENT, WRAP_CONTENT)
      }
    }
  }

  private fun MenuItem.tintedIcon(): Drawable? {
    val icon = icon ?: return null
    icon.mutate()
    icon.setTintList(iconTintList)

    if (SDK_INT >= 29) {
      icon.setTintBlendMode(iconTintBlendMode)
    } else {
      icon.setTintMode(iconTintMode)
    }
    return icon
  }

  private fun createIndicatorIcon(): Drawable {
    return context.getDrawable(R.drawable.ic_round_arrow_right_8, Color.GRAY)
  }

  protected fun handleOnClick(item: MenuItem) {
    if (item.subMenu != null) {
      showMenu(item.subMenu)
    } else {
      onMenuItemClickListener?.onMenuItemClick(item)
      dismiss()
    }
  }

  private fun showMenu(menu: Menu) {
    (contentView as HeightAnimatableViewFlipper).addView(menu.createLayout())
  }
}

private fun TextView.setCompoundDrawables(start: Drawable?, end: Drawable?) {
  setCompoundDrawablesRelativeWithIntrinsicBounds(start, null, end, null)
}
