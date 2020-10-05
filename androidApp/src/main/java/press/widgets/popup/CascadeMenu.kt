package press.widgets.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
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
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.iterator

@SuppressLint("RestrictedApi")
open class CascadeMenu @JvmOverloads constructor(
  private val context: Context,
  private val styler: Styler,
  private val fixedWidth: Int = context.dip(200),
  private val defStyleAttr: Int = android.R.style.Widget_Material_PopupMenu
) : CascadePopupWindow(context, defStyleAttr) {

  val menu: Menu = MenuBuilder(context)
  var onMenuItemClickListener: OnMenuItemClickListener? = null

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
      background = styler.background(AppCompatResources.getDrawable(context, themeAttrs.popupBackgroundRes)!!)
      clipToOutline = true
    }
    showMenu(menu)
  }

  private fun showMenu(menu: Menu) {
    (contentView as HeightAnimatableViewFlipper).addView(createLayout(menu))
  }

  private fun createLayout(menu: Menu): View {
    return LinearLayout(context).apply {
      orientation = VERTICAL
      layoutParams = LayoutParams(fixedWidth, WRAP_CONTENT)

      if (menu is SubMenu) {
        val headerView = MenuHeaderViewHolder(parent = this, menu)
        styler.menuTitle(headerView.textView, menu.item)
        addView(headerView.layout)
      }

      val hasSubMenu = menu.items.any { it.hasSubMenu() }
      for (item in menu) {
        val view = MenuItemViewHolder(
            item = item,
            parent = this,
            hasSubMenuSiblings = hasSubMenu
        )
        view.layout.setBackgroundResource(themeAttrs.touchFeedbackRes)
        styler.menuItem(view.titleView, item)
        view.layout.setOnClickListener { handleOnClick(item) }
        addView(view.layout, MATCH_PARENT, WRAP_CONTENT)
      }
    }
  }

  protected fun handleOnClick(item: MenuItem) {
    if (item.hasSubMenu()) {
      showMenu(item.subMenu)
    } else {
      onMenuItemClickListener?.onMenuItemClick(item)
      dismiss()
    }
  }
}

@OptIn(ExperimentalStdlibApi::class)
private val Menu.items: List<MenuItem>
  get() {
    return this.iterator().asSequence().toList()
  }

private fun Context.dip(dp: Int): Int {
  val metrics = resources.displayMetrics
  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
}
