package me.saket.press.shared.ui

interface Navigator {
  // https://www.urbandictionary.com/define.php?term=lfg
  fun lfg(screen: ScreenKey)
  fun goBack()
}

interface ScreenKey
