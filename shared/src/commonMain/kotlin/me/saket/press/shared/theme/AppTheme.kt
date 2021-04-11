package me.saket.press.shared.theme

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.subject.behavior.BehaviorSubject
import com.badoo.reaktive.subject.publish.PublishSubject
import me.saket.press.shared.preferences.UserPreferences
import me.saket.press.shared.rx.mergeWith
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
  private val userPrefs: UserPreferences,
  startWithDarkMode: Boolean
) {
  private val onPreChange = PublishSubject<ThemePalette>()
  private val stream = BehaviorSubject(userPrefs.determinePaletteFor(darkMode = startWithDarkMode))
  val palette: ThemePalette get() = stream.value

  protected var isSystemInDarkMode: Boolean = startWithDarkMode
    set(value) {
      field = value
      stream.onNext(userPrefs.determinePaletteFor(darkMode = value))
    }

  // This stream gets garbage collected if it's not stored in a class property.
  private val disposable = userPrefs.themeSwitchingMode.listen()
    .mergeWith(userPrefs.darkThemePalette.listen())
    .mergeWith(userPrefs.lightThemePalette.listen())
    .subscribe {
      val newPalette = userPrefs.determinePaletteFor(isSystemInDarkMode)
      if (palette != newPalette) {
        onPreChange.onNext(newPalette)
        stream.onNext(newPalette)
      }
    }

  internal fun listen(): Observable<ThemePalette> {
    return stream
  }

  internal fun listenPreChange(): Observable<ThemePalette> {
    return onPreChange
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

  private fun UserPreferences.determinePaletteFor(darkMode: Boolean): ThemePalette {
    return when (themeSwitchingMode.get()!!) {
      AlwaysDark -> darkThemePalette.get()!!
      NeverDark -> lightThemePalette.get()!!
      MatchSystem -> when {
        darkMode -> darkThemePalette.get()!!
        else -> lightThemePalette.get()!!
      }
    }
  }
}
