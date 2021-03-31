package me.saket.press.shared.preferences

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import me.saket.press.shared.theme.UiStyles.FontFamily
import me.saket.press.shared.theme.UiStyles.FontFamily.WORK_SANS

inline class AutoCorrectEnabled(val enabled: Boolean)

@OptIn(ExperimentalSettingsApi::class)
class UserPreferences(settings: ObservableSettings) {
  val autoCorrectEnabled = Setting.create(
    settings = settings,
    key = "autocorrect_enabled",
    from = { AutoCorrectEnabled(it.toBoolean()) },
    to = { it.enabled.toString() },
    defaultValue = AutoCorrectEnabled(true)
  )

  val fontFamily = Setting.create(
    settings = settings,
    key = "typeface",
    from = { FontFamily.valueOf(it) },
    to = { it.name },
    defaultValue = WORK_SANS
  )
}
