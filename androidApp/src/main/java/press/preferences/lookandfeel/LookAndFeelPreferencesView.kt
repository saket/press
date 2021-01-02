package press.preferences.lookandfeel

import android.content.Context
import android.util.AttributeSet
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import me.saket.press.R
import me.saket.press.shared.localization.strings
import press.extensions.createRippleDrawable
import press.theme.themeAware
import press.widgets.PressToolbar

class LookAndFeelPreferencesView(context: Context) : ContourLayout(context) {
  private val toolbar = PressToolbar(context).apply {
    title = context.strings().prefs.category_title_look_and_feel
  }

  init {
    id = R.id.lookandfeel_preferences_view
    themeAware {
      setBackgroundColor(it.window.backgroundColor)
    }

    toolbar.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
  }
}
