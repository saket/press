package press.widgets

import android.content.Context
import androidx.appcompat.widget.Toolbar
import me.saket.press.R.drawable
import me.saket.press.shared.localization.strings
import press.theme.themeAware
import press.extensions.getDrawable

class PressToolbar(context: Context) : Toolbar(context) {
  init {
    navigationContentDescription = context.strings().common.closeNavIconDescription
    themeAware {
      navigationIcon = context.getDrawable(drawable.ic_close_24dp, it.accentColor)
    }
  }
}
