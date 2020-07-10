package press.editor

import android.content.Context
import android.graphics.Point
import android.text.style.URLSpan
import android.view.View
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import me.saket.press.shared.localization.strings
import press.extensions.reflect

class UrlPopupMenu(
  context: Context,
  anchor: View,
  url: String
) : PopupMenu(context, anchor) {

  init {
    val openString = context.strings().editor.open_url
    val editString = context.strings().editor.edit_url

    menu.add(openString)
    menu.add(editString)

    setOnMenuItemClickListener { item ->
      if (item.title == openString) {
        URLSpan(url).onClick(anchor)
      }
      false
    }
  }

  fun showAt(location: Point) {
    val popupHelper = reflect<PopupMenu>().field("mPopup").get(this) as MenuPopupHelper
    val tryShowMethod = reflect<MenuPopupHelper>().method("tryShow", Int::class.java, Int::class.java)
    tryShowMethod.invoke(popupHelper, location.x, location.y)
  }
}
