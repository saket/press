package me.saket.press.shared.theme

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import me.saket.press.shared.preferences.UserPreferences
import me.saket.press.shared.theme.palettes.CascadeThemePalette
import me.saket.press.shared.theme.palettes.CityLightsThemePalette
import me.saket.press.shared.theme.palettes.DraculaThemePalette
import me.saket.press.shared.theme.palettes.MinimalDarkThemePalette
import me.saket.press.shared.theme.palettes.MinimalLightThemePalette
import me.saket.press.shared.theme.palettes.PureBlackThemePalette
import me.saket.press.shared.theme.palettes.SolarizedLightThemePalette

class AppTheme(userPreferences: UserPreferences) {
  private val stream: BehaviorSubject<ThemePalette>
  val palette get() = stream.value

  init {
    val currentTheme = userPreferences.darkThemePalette.get()!!
    stream = BehaviorSubject(currentTheme)
  }

  fun change(palette: ThemePalette) {
    stream.onNext(palette)
  }

  internal fun listen(): Observable<ThemePalette> = stream

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
