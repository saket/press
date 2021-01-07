package press.preferences.lookandfeel

import android.content.Context
import android.util.AttributeSet
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.preferences.UserPreferences
import press.preferences.PreferenceRowView
import press.theme.themeAware
import press.widgets.PressToolbar

class LookAndFeelPreferencesView @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  userPreferences: UserPreferences
) : ContourLayout(context) {

  private val toolbar = PressToolbar(context).apply {
    title = context.strings().prefs.category_title_look_and_feel
  }

  private val fontFamilyView = PreferenceRowView(context).apply {
    render(
      setting = userPreferences.fontFamily,
      title = context.strings().prefs.lookandfeel_fontfamily,
      subtitle = { it!!.displayName }
    )
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
    fontFamilyView.layoutBy(
      x = matchParentX(),
      y = topTo { toolbar.bottom() }
    )
  }
}
