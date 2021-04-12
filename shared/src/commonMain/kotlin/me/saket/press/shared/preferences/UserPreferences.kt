package me.saket.press.shared.preferences

import com.russhwolf.settings.ObservableSettings
import me.saket.press.shared.theme.ThemeSwitchingMode
import me.saket.press.shared.theme.ThemeSwitchingMode.MatchSystem
import me.saket.press.shared.theme.UiStyles.Typeface
import me.saket.press.shared.theme.UiStyles.Typeface.WorkSans
import me.saket.press.shared.theme.palettes.CascadeThemePalette
import me.saket.press.shared.theme.palettes.CityLightsThemePalette
import me.saket.press.shared.theme.palettes.DraculaThemePalette
import me.saket.press.shared.theme.palettes.MinimalDarkThemePalette
import me.saket.press.shared.theme.palettes.MinimalLightThemePalette
import me.saket.press.shared.theme.palettes.PureBlackThemePalette
import me.saket.press.shared.theme.palettes.SolarizedLightThemePalette
import me.saket.press.shared.theme.palettes.ThemePalette

inline class AutoCorrectEnabled(val enabled: Boolean)

class UserPreferences(settings: ObservableSettings) {
  val autoCorrectEnabled = settings.setting(
    key = "autocorrect_enabled",
    from = { AutoCorrectEnabled(it.toBoolean()) },
    to = { it.enabled.toString() },
    defaultValue = AutoCorrectEnabled(true)
  )

  val typeface = settings.setting(
    key = "typeface",
    from = { Typeface.valueOf(it) },
    to = { it.name },
    defaultValue = WorkSans
  )

  val themeSwitchingMode = settings.setting(
    key = "theme_switching_mode",
    from = { ThemeSwitchingMode.valueOf(it) },
    to = { it.name },
    defaultValue = MatchSystem
  )

  val lightThemePalette = settings.setting(
    key = "light_theme_palette",
    from = { ThemePaletteSerializer.fromString(it, default = CascadeThemePalette) },
    to = { ThemePaletteSerializer.toString(it) },
    defaultValue = CascadeThemePalette
  )

  val darkThemePalette = settings.setting(
    key = "dark_theme_palette",
    from = { ThemePaletteSerializer.fromString(it, default = DraculaThemePalette) },
    to = { ThemePaletteSerializer.toString(it) },
    defaultValue = DraculaThemePalette
  )

  private object ThemePaletteSerializer {
    fun toString(palette: ThemePalette): String {
      return when (palette) {
        CascadeThemePalette -> "cascade"
        CityLightsThemePalette -> "city_lights"
        DraculaThemePalette -> "dracula"
        MinimalDarkThemePalette -> "minimal_dark"
        MinimalLightThemePalette -> "minimal_light"
        PureBlackThemePalette -> "pure_black"
        SolarizedLightThemePalette -> "solarized_light"
      }
    }

    fun fromString(serialized: String, default: ThemePalette): ThemePalette {
      return when (serialized) {
        "cascade" -> CascadeThemePalette
        "city_lights" -> CityLightsThemePalette
        "dracula" -> DraculaThemePalette
        "minimal_dark" -> MinimalDarkThemePalette
        "minimal_light" -> MinimalLightThemePalette
        "pure_black" -> PureBlackThemePalette
        "solarized_light" -> SolarizedLightThemePalette
        else -> default
      }
    }
  }
}
