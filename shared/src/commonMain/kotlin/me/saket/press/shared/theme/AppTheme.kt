package me.saket.press.shared.theme

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import me.saket.press.shared.preferences.UserPreferences
import me.saket.press.shared.theme.ThemeSwitchingMode.AlwaysDark
import me.saket.press.shared.theme.ThemeSwitchingMode.MatchSystem
import me.saket.press.shared.theme.ThemeSwitchingMode.NeverDark
import me.saket.press.shared.theme.palettes.CascadeThemePalette
import me.saket.press.shared.theme.palettes.CityLightsThemePalette
import me.saket.press.shared.theme.palettes.DraculaThemePalette
import me.saket.press.shared.theme.palettes.MinimalDarkThemePalette
import me.saket.press.shared.theme.palettes.MinimalLightThemePalette
import me.saket.press.shared.theme.palettes.PureBlackThemePalette
import me.saket.press.shared.theme.palettes.SolarizedLightThemePalette

abstract class AppTheme(
  private val userPreferences: UserPreferences,
  startWithDarkMode: Boolean
) {
  private val stream = BehaviorSubject(userPreferences.palette(darkMode = startWithDarkMode))
  val palette: ThemePalette get() = stream.value

  fun change(palette: ThemePalette) {
    stream.onNext(palette)
  }

  internal fun listen(): Observable<ThemePalette> {
    return stream
  }

  protected fun setDarkModeEnabled(darkMode: Boolean) {
    stream.onNext(userPreferences.palette(darkMode))
  }

  fun lightThemePalettes(): List<ThemePalette> {
    return listOf(
      SolarizedLightThemePalette, MinimalLightThemePalette, CascadeThemePalette
    )
  }

  fun darkThemePalettes(): List<ThemePalette> {
    return listOf(
      DraculaThemePalette, MinimalDarkThemePalette, CityLightsThemePalette, PureBlackThemePalette
    )
  }
}

private fun UserPreferences.palette(darkMode: Boolean): ThemePalette {
  return when (themeSwitchingMode.get()!!) {
    AlwaysDark -> darkThemePalette.get()!!
    NeverDark -> lightThemePalette.get()!!
    MatchSystem -> when {
      darkMode -> darkThemePalette.get()!!
      else -> lightThemePalette.get()!!
    }
  }
}
