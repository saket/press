@file:SuppressLint("RestrictedApi")

package press.widgets.popup

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.MenuItem
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu

internal fun Context.dip(dp: Int): Int {
  val metrics = resources.displayMetrics
  return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
}

internal fun MenuBuilder.setCallback(listener: PopupMenu.OnMenuItemClickListener?) {
  setCallback(object : MenuBuilder.Callback {
    override fun onMenuModeChange(menu: MenuBuilder) = Unit
    override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean =
      listener?.onMenuItemClick(item) ?: false
  })
}
