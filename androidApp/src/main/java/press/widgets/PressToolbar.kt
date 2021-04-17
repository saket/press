package press.widgets

import android.content.Context
import androidx.appcompat.widget.Toolbar
import androidx.core.view.updatePaddingRelative
import me.saket.press.R
import me.saket.press.shared.localization.strings
import press.extensions.getDrawable
import press.navigation.navigator
import press.theme.themeAware

@Suppress("LeakingThis")
open class PressToolbar(context: Context, showNavIcon: Boolean = true) : Toolbar(context) {
  init {
    if (showNavIcon) {
      navigationContentDescription = context.strings().common.close_nav_icon_contentdescription
      themeAware {
        super.setNavigationIcon(context.getDrawable(R.drawable.ic_close_24dp, it.accentColor))
      }
      setNavigationOnClickListener {
        navigator().goBack()
      }
    }

    // Match Toolbar's icon and title margins with list items across Press.
    // Icon's left = 16dp.
    // Icon's width = 52dp (set in styles.xml).
    // Title's left = if (icon != null) 56dp else 16dp.
    if (showNavIcon) {
      contentInsetStartWithNavigation = dp(4)
    } else {
      setContentInsetsRelative(dp(16), dp(16))
    }

    // Toolbar sets 8dp of margins on larger displays. Press prefers
    // to keep its margins consistent with spacings of its list items.
    updatePaddingRelative(start = 0, end = 0)
  }
}
