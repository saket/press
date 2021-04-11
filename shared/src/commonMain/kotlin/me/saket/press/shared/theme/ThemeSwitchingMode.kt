package me.saket.press.shared.theme

import me.saket.press.shared.localization.Strings

enum class ThemeSwitchingMode {
  AlwaysDark,
  NeverDark,
  MatchSystem;

  fun displayName(strings: Strings.Preferences): String {
    return when (this) {
      AlwaysDark -> strings.theme_themeMode_always_dark
      NeverDark -> strings.theme_themeMode_never_dark
      MatchSystem -> strings.theme_themeMode_match_system
    }
  }
}
