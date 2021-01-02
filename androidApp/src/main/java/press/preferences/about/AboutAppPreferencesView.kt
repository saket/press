package press.preferences.about

import android.content.Context
import com.squareup.contour.ContourLayout
import me.saket.press.R
import me.saket.press.shared.localization.strings
import press.theme.themeAware
import press.widgets.PressToolbar

class AboutAppPreferencesView(context: Context) : ContourLayout(context) {
  private val toolbar = PressToolbar(context).apply {
    title = context.strings().prefs.category_title_about_app
  }

  init {
    id = R.id.aboutapp_preferences_view
    themeAware {
      setBackgroundColor(it.window.backgroundColor)
    }

    toolbar.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
  }
}
