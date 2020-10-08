@file:SuppressLint("RestrictedApi")

package press.widgets.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.View.SCROLLBARS_INSIDE_OVERLAY
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.MenuRes
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.appcompat.view.menu.SubMenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

open class CascadeMenu @JvmOverloads constructor(
  private val context: Context,
  private val anchor: View,
  var gravity: Int = Gravity.NO_GRAVITY,
  private val styler: Styler = Styler(),
  private val fixedWidth: Int = context.dip(200),
  private val defStyleAttr: Int = android.R.style.Widget_Material_PopupMenu
) {

  val menu: Menu = MenuBuilder(context)
  private val popup = CascadePopupWindow(context, defStyleAttr)
  private val themeAttrs get() = popup.themeAttrs

  class Styler(
    val background: (Drawable) -> Drawable = { it },
    val menuList: (RecyclerView) -> Unit = {},
    val menuTitle: (MenuHeaderViewHolder) -> Unit = {},
    val menuItem: (MenuItemViewHolder) -> Unit = {}
  )

  fun show() {
    popup.contentView.background = styler.background(popup.contentView.background)

    showMenu(menu, goingForward = true)
    popup.showAsDropDown(anchor, 0, 0, gravity)
  }

  fun goBackFrom(item: MenuItem) {
    println("item: $item")
    check(item is SubMenuBuilder)
    check(item.parentMenu is SubMenu) { "todo: doc" }
    showMenu(item.parentMenu, goingForward = false)
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

      // Opaque background to avoid cross-drawing
      // of menus during entry/exit animation.
      if (menu is SubMenu) {
        background = themeAttrs.popupBackground(context)
      }

      // PopupWindow doesn't allow its content to have a fixed
      // width so any fixed size must be set on its children instead.
      layoutParams = LayoutParams(fixedWidth, WRAP_CONTENT)
    }
    popup.contentView.show(menuList, goingForward)
  }

  protected open fun handleTitleClick(menu: SubMenuBuilder) {
    showMenu(menu.parentMenu, goingForward = false)
  }

  protected open fun handleItemClick(item: MenuItem) {
    if (item.hasSubMenu()) {
      showMenu(item.subMenu, goingForward = true)
      return
    }

    (item as MenuItemImpl).invoke()
    popup.dismiss()
  }

// === APIs to maintain compatibility with PopupMenu === //

  fun inflate(@MenuRes menuRes: Int) =
    SupportMenuInflater(context).inflate(menuRes, menu)

  fun setOnMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener?) =
    (menu as MenuBuilder).setCallback(listener)

  fun dismiss() =
    popup.dismiss()
}

internal fun Context.dip(dp: Int): Int {
  val metrics = resources.displayMetrics
  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
}

private fun MenuBuilder.setCallback(listener: PopupMenu.OnMenuItemClickListener?) {
  setCallback(object : MenuBuilder.Callback {
    override fun onMenuModeChange(menu: MenuBuilder) = Unit
    override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean =
      listener?.onMenuItemClick(item) ?: false
  })
}
