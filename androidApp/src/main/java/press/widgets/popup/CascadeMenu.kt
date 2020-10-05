package press.widgets.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnMenuItemClickListener
import android.view.SubMenu
import android.view.View
import android.view.View.SCROLLBARS_INSIDE_OVERLAY
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.SubMenuBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
    val menuList: (RecyclerView) -> Unit = {},
    val menuTitle: (MenuHeaderViewHolder) -> Unit = {},
    val menuItem: (MenuItemViewHolder) -> Unit = {}
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
      clipToOutline = true
      background = styler.background(themeAttrs.popupBackground())
    }
    showMenu(menu, goingForward = true)
  }

  private fun showMenu(menu: Menu, goingForward: Boolean) {
    val menuList = RecyclerView(context).apply {
      isVerticalScrollBarEnabled = true
      scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
      layoutManager = LinearLayoutManager(context)
      styler.menuList(this)
      adapter = CascadeMenuAdapter(menu, styler, themeAttrs,
          onTitleClick = { handleTitleClick(it) },
          onItemClick = { handleItemClick(it) }
      )

      // Give an opaque background to avoid cross-drawing of menus during animation.
      if (menu is SubMenu) {
        background = themeAttrs.popupBackground()
      }

      // PopupWindow doesn't allow its content to have a fixed
      // width so any fixed size must be set on its children instead.
      layoutParams = LayoutParams(fixedWidth, WRAP_CONTENT)
    }

    val flipper = contentView as HeightAnimatableViewFlipper
    flipper.show(menuList, goingForward)
  }

  protected fun handleTitleClick(menu: SubMenuBuilder) {
    showMenu(menu.parentMenu, goingForward = false)
  }

  protected fun handleItemClick(item: MenuItem) {
    if (item.hasSubMenu()) {
      showMenu(item.subMenu, goingForward = true)
    } else {
      onMenuItemClickListener?.onMenuItemClick(item)
      dismiss()
    }
  }
}

internal fun Context.dip(dp: Int): Int {
  val metrics = resources.displayMetrics
  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
}
