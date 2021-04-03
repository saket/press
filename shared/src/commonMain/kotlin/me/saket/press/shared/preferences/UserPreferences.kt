package me.saket.press.shared.preferences

import com.russhwolf.settings.ObservableSettings
import me.saket.press.shared.theme.ThemeSwitchingMode
import me.saket.press.shared.theme.UiStyles.FontFamily
import me.saket.press.shared.theme.UiStyles.FontFamily.WORK_SANS

inline class AutoCorrectEnabled(val enabled: Boolean)

class UserPreferences(settings: ObservableSettings) {
  val autoCorrectEnabled = settings.setting(
    key = "autocorrect_enabled",
    from = { AutoCorrectEnabled(it.toBoolean()) },
    to = { it.enabled.toString() },
    defaultValue = AutoCorrectEnabled(true)
  )

  val fontFamily = settings.setting(
    key = "typeface",
    from = { FontFamily.valueOf(it) },
    to = { it.name },
    defaultValue = WORK_SANS
  )

  val themeSwitchingMode = settings.setting(
    key = "theme_switching_mode",
    from = { ThemeSwitchingMode.valueOf(it) },
    to = { it.name },
    defaultValue = ThemeSwitchingMode.MatchSystem
  )
}
