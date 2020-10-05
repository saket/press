package press.widgets.popup

import android.content.Context
import android.view.LayoutInflater
import android.view.SubMenu
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import me.saket.press.R

/**
 * Layout for a sub-menu header.
 */
class MenuHeaderViewHolder(
  private val parent: ViewGroup,
  private val menu: SubMenu
) {
  private val context: Context get() = parent.context
  val layout = LayoutInflater.from(context).inflate(R.layout.abc_popup_menu_header_item_layout, parent, false)
  val textView: TextView = layout.findViewById(android.R.id.title)

  init {
    check(menu is MenuBuilder)
    textView.text = menu.headerTitle
    textView.isEnabled = false
  }
}
