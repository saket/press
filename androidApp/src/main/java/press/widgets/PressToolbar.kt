package press.widgets

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import me.saket.press.R.drawable
import me.saket.press.shared.localization.Strings
import me.saket.press.shared.localization.strings
import press.theme.themeAware

class PressToolbar(context: Context) : Toolbar(context) {
  init {
    navigationContentDescription = context.strings().common.closeNavIconDescription
    navigationIcon = AppCompatResources.getDrawable(context, drawable.ic_close_24dp)
    themeAware {
      setBackgroundColor(it.window.editorBackgroundColor)
    }
  }
}
