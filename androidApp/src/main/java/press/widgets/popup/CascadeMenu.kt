package press.widgets.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Color.WHITE
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.view.Gravity
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
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.iterator
import androidx.core.view.setPadding
import androidx.core.view.updatePadding

// TODO
//  - window margins.
@SuppressLint("RestrictedApi")
class CascadeMenu(
  private val context: Context,
  fixedWidthInDp: Int = 200,
  private val gravity: Int = Gravity.START or Gravity.CENTER_VERTICAL
) : CascadePopupWindow(context) {
  val menu: Menu = MenuBuilder(context)
  var onMenuItemClickListener: OnMenuItemClickListener? = null
  private val fixedWidth = fixedWidthInDp.dip

  class Styler(
    val background: (Drawable) -> Drawable = { it },
    val menuTitle: (TextView, MenuItem) -> Unit = { _, _ -> },
    val menuItem: (TextView, MenuItem) -> Unit = { _, _ -> }
  )

  fun show(anchor: View, styler: Styler) {
    contentView = HeightAnimatableViewFlipper(context).apply {
      background = styler.background(PaintDrawable(WHITE).apply { setCornerRadius(20f.dip) })
      clipToOutline = true

      val onClick = { item: MenuItem ->
        if (item.subMenu != null) {
          showView(createMenuView(item.subMenu, styler, onClick = {
            check(it.subMenu == null) { "todo: support nested menus" }
            onMenuItemClickListener?.onMenuItemClick(it)
            dismiss()
          }))

        } else {
          onMenuItemClickListener?.onMenuItemClick(item)
          dismiss()
        }
      }
      showView(createMenuView(menu, styler, onClick))
    }

    if (false) {
      PopupMenu(context, anchor).also {
        it.menu.add("Open")
        it.menu.addSubMenu("Remove").also { sub ->
          sub.add("Confirm remove")
          sub.add("Wait no")
        }
        it.menu.add("Logs")
        it.show()
      }
    } else {
      showAsDropDown(anchor, 0, 0, gravity)
    }
  }

  private fun createMenuView(
    menu: Menu,
    styler: Styler,
    onClick: (MenuItem) -> Unit
  ): View {
    return LinearLayout(context).apply {
      orientation = VERTICAL
      layoutParams = LayoutParams(fixedWidth, WRAP_CONTENT)

      if (menu is SubMenu) {
        addView(TextView(context).also {
          it.textSize = 14f
          it.text = menu.item.title
          it.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
          it.updatePadding(left = 16.dip, right = 16.dip, top = 16.dip, bottom = 8.dip)
          styler.menuTitle(it, menu.item)
        })
      }

      for (item in menu) {
        addView(TextView(context).also {
          it.textSize = 16f
          it.text = item.title
          it.background = createRippleDrawable(Color.GRAY)
          it.setPadding(16.dip)
          styler.menuItem(it, item)
          it.setOnClickListener { onClick(item) }
        }, MATCH_PARENT, WRAP_CONTENT)
      }
    }
  }
}

