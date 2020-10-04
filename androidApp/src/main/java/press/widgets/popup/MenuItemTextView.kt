package press.widgets.popup

import android.content.Context
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.ListMenuItemView
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import me.saket.press.R
import kotlin.LazyThreadSafetyMode.NONE

class MenuItemViewHolder(private val context: Context, parent: ViewGroup) {
  val layout: ListMenuItemView = LayoutInflater
      .from(context)
      .inflate(R.layout.abc_popup_menu_item_layout, parent, false) as ListMenuItemView

  val titleView: TextView = layout.findViewById(R.id.title)
  private val contentView: View = layout.findViewById(R.id.content)
  private val iconView: ImageView by lazy(NONE) { layout.findViewById(R.id.icon) }
  private val arrowView: ImageView = layout.findViewById(R.id.submenuarrow)

  private val Int.dip: Int
    get() {
      val metrics = context.resources.displayMetrics
      return TypedValue.applyDimension(COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
    }

  fun render(item: MenuItem) {
    check(item is MenuItemImpl)
    layout.setForceShowIcon(true)
    layout.initialize(item, 0)
    layout.setGroupDividerEnabled(false)

    titleView.textSize = 16f

    iconView.updateLayoutParams<MarginLayoutParams> {
      leftMargin = 14.dip
    }

    if (item.hasSubMenu()) {
      arrowView.setImageResource(R.drawable.ic_round_arrow_right_24)
      contentView.updatePaddingRelative(end = 4.dip)
    }
  }
}
