package press.preferences.theme

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.LinearLayout.SHOW_DIVIDER_END
import android.widget.LinearLayout.SHOW_DIVIDER_MIDDLE
import android.widget.LinearLayout.VERTICAL
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import me.saket.cascade.CascadePopupMenu
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.preferences.UserPreferences
import me.saket.press.shared.theme.AppTheme
import me.saket.press.shared.theme.ThemeSwitchingMode
import press.preferences.TwoLinePreferenceView
import press.theme.pressCascadeStyler
import press.theme.themeAware
import press.widgets.DividerDrawable
import press.widgets.PressToolbar
import press.widgets.dp

class ThemePreferencesView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  userPreferences: UserPreferences,
  appTheme: AppTheme,
) : ContourLayout(context) {

  private val strings get() = context.strings().prefs

  private val toolbar = PressToolbar(context).apply {
    title = context.strings().prefs.category_title_theme
  }

  private val preferenceList = LinearLayout(context).apply {
    orientation = VERTICAL
    showDividers = SHOW_DIVIDER_MIDDLE or SHOW_DIVIDER_END
    updatePadding(bottom = dp(24))
    themeAware { dividerDrawable = DividerDrawable(it.separator) }
  }

  private val themeModeView = TwoLinePreferenceView(context)

  private val lightThemePaletteView = ThemePalettePickerView(
    context,
    title = "Light theme",
    palettes = appTheme.lightThemePalettes(),
    setting = userPreferences.lightThemePalette
  )
  private val darkThemePaletteView = ThemePalettePickerView(
    context,
    title = "Dark theme",
    palettes = appTheme.darkThemePalettes(),
    setting = userPreferences.darkThemePalette
  )

  init {
    id = R.id.theme_preferences_view
    lightThemePaletteView.paletteListView.id = R.id.themepreferences_light_palette_list
    darkThemePaletteView.paletteListView.id = R.id.themepreferences_dark_palette_list

    themeAware {
      setBackgroundColor(it.window.backgroundColor)
    }

    toolbar.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
    NestedScrollView(context).apply {
      addView(preferenceList)
      layoutBy(
        x = matchParentX(),
        y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
      )
    }

    preferenceList.let {
      it.addView(themeModeView)
      it.addView(lightThemePaletteView)
      it.addView(darkThemePaletteView)
    }

    themeModeView.render(
      setting = userPreferences.themeSwitchingMode,
      title = strings.theme_themeMode_title,
      subtitle = { it.displayName(strings) },
      onClick = {
        CascadePopupMenu(context, anchor = themeModeView, styler = pressCascadeStyler()).apply {
          ThemeSwitchingMode.values().forEach { mode ->
            menu.add(mode.displayName(strings)).setOnMenuItemClickListener {
              userPreferences.themeSwitchingMode.set(mode)
              true
            }
          }
        }.show()
      }
    )
  }
}
