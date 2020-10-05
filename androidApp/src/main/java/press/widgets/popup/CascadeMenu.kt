package press.widgets.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnMenuItemClickListener
import android.view.View
import android.view.View.SCROLLBARS_INSIDE_OVERLAY
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
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
      background = styler.background(AppCompatResources.getDrawable(context, themeAttrs.popupBackgroundRes)!!)
      clipToOutline = true
    }
    showMenu(menu)
  }

  private fun showMenu(menu: Menu) {
    val menuList = RecyclerView(context).apply {
      isVerticalScrollBarEnabled = true
      scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
      layoutManager = LinearLayoutManager(context)
      adapter = CascadeMenuAdapter(menu, styler, themeAttrs, onClick = { handleOnClick(it) })
      styler.menuList(this)
    }

    val flipper = contentView as HeightAnimatableViewFlipper
    flipper.addView(menuList, LayoutParams(fixedWidth, WRAP_CONTENT))
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

internal fun Context.dip(dp: Int): Int {
  val metrics = resources.displayMetrics
  return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
}
