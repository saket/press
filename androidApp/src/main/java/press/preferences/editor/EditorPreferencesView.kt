package press.preferences.editor

import android.content.Context
import com.squareup.contour.ContourLayout
import me.saket.press.R
import me.saket.press.shared.localization.strings
import press.theme.themeAware
import press.widgets.PressToolbar

class EditorPreferencesView(context: Context) : ContourLayout(context) {
  private val toolbar = PressToolbar(context).apply {
    title = context.strings().prefs.category_title_editor
  }

  init {
    id = R.id.editor_preferences_view
    themeAware {
      setBackgroundColor(it.window.backgroundColor)
    }

    toolbar.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
  }
}
