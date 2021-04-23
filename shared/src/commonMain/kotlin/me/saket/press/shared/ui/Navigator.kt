package me.saket.press.shared.ui

import me.saket.press.shared.AndroidParcel

interface Navigator {
  // https://www.urbandictionary.com/define.php?term=lfg
  fun lfg(screen: ScreenKey)
  fun clearTopAndLfg(screen: ScreenKey)
  fun splitScreenAndLfg(screen: ScreenKey)
  fun goBack(result: ScreenResult? = null)
  fun intentLauncher(): IntentLauncher

  /** Currently called in response to theme changes. */
  fun recreateScreens()
}

interface ScreenKey : AndroidParcel
