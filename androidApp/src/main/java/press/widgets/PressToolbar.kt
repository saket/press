package press.widgets

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import me.saket.press.R.drawable
import me.saket.press.shared.localization.Strings
import press.theme.themeAware

class PressToolbar(context: Context, strings: Strings) : Toolbar(context) {
  init {
    navigationContentDescription = strings.common.closeNavIconDescription
    navigationIcon = AppCompatResources.getDrawable(context, drawable.ic_close_24dp)
    themeAware {
      setBackgroundColor(it.window.editorBackgroundColor)
    }
  }
}
