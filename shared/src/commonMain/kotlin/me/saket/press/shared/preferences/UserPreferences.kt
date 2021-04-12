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
    private val paletteToNames = mapOf(
      CascadeThemePalette to "cascade",
      CityLightsThemePalette to "city_lights",
      DraculaThemePalette to "dracula",
      MinimalDarkThemePalette to "minimal_dark",
      MinimalLightThemePalette to "minimal_light",
      PureBlackThemePalette to "pure_black",
      SolarizedLightThemePalette to "solarized_light",
    )

    fun toString(palette: ThemePalette): String {
      return paletteToNames[palette]!!
    }

    /**
     * @param default will be used in case of de-serialization errors.
     */
    fun fromString(serialized: String, default: ThemePalette): ThemePalette {
      return paletteToNames.entries
        .firstOrNull { (_, serializedName) -> serialized == serializedName }
        ?.key ?: default
    }
  }
}
