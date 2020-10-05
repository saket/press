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
import androidx.annotation.Px
import androidx.appcompat.view.menu.ListMenuItemView
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import me.saket.press.R
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Layout for a menu item.
 */
class MenuItemViewHolder(
  private val parent: ViewGroup,
  private val item: MenuItem,
  private val hasSubMenuSiblings: Boolean
) {
  private val context: Context get() = parent.context
  val layout: ListMenuItemView = LayoutInflater
      .from(context)
      .inflate(R.layout.abc_popup_menu_item_layout, parent, false) as ListMenuItemView

  val titleView: TextView = layout.findViewById(R.id.title)
  val titleContainerView: ViewGroup = titleView.parent as ViewGroup

  val contentView: View = layout.findViewById(R.id.content)
  val iconView: ImageView by lazy(NONE) { layout.findViewById(R.id.icon) }
  val subMenuArrowView: ImageView = layout.findViewById(R.id.submenuarrow)

  private val Int.dip: Int
    get() {
      val metrics = context.resources.displayMetrics
      return TypedValue.applyDimension(COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
    }

  init {
    check(item is MenuItemImpl)
    layout.setForceShowIcon(true)
    layout.initialize(item, 0)
    layout.setGroupDividerEnabled(false)

    titleView.textSize = 16f

    if (item.hasSubMenu()) {
      subMenuArrowView.setImageResource(R.drawable.ic_round_arrow_right_24)
    }

    subMenuArrowView.updateMargin(start = 0.dip)
    setContentSpacing(
        start = if (item.icon != null) 12.dip else 14.dip,
        end = when {
          item.hasSubMenu() -> 4.dip
          hasSubMenuSiblings -> 28.dip
          else -> 14.dip
        },
        iconSpacing = 14.dip
    )
  }

  fun setContentSpacing(@Px start: Int, @Px end: Int, @Px iconSpacing: Int) {
    if (item.icon != null) {
      iconView.updateMargin(start = start)
      titleContainerView.updateMargin(start = iconSpacing)
      contentView.updatePaddingRelative(end = end)

    } else {
      titleContainerView.updateMargin(start = start)
      contentView.updatePaddingRelative(end = end)
    }
  }
}

private fun View.updateMargin(start: Int) {
  updateLayoutParams<MarginLayoutParams> {
    marginStart = start
  }
}
