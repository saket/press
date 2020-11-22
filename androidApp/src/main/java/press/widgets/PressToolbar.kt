package press.widgets

import android.content.Context
import androidx.appcompat.widget.Toolbar
import me.saket.press.R
import me.saket.press.shared.localization.strings
import press.extensions.getDrawable
import press.navigation.navigator
import press.theme.themeAware

class PressToolbar(context: Context, showNavIcon: Boolean = true) : Toolbar(context) {
  init {
    if (showNavIcon) {
      navigationContentDescription = context.strings().common.close_nav_icon_description
      themeAware {
        super.setNavigationIcon(context.getDrawable(R.drawable.ic_close_24dp, it.accentColor))
      }
      setNavigationOnClickListener {
        navigator().goBack()
      }
    }
  }
}
