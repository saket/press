package press.widgets.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.iterator

/**
 * TODO
 *  - window margins.
 *  - go back
 *  - scrollable container
 *  - use theme colors
 *    - text color
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
        val headerView = MenuHeaderViewHolder(context, parent = this)
        headerView.render(menu)
        styler.menuTitle(headerView.textView, menu.item)
        addView(headerView.layout)
      }

      for (item in menu) {
        val itemView = MenuItemViewHolder(context, parent = this).also {
          it.render(item)
          it.layout.setBackgroundResource(themeAttrs.touchFeedbackRes)
          styler.menuItem(it.titleView, item)
          it.layout.setOnClickListener { handleOnClick(item) }
        }
        addView(itemView.layout, MATCH_PARENT, WRAP_CONTENT)
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
