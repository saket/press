package me.saket.press.shared.ui

fun interface Navigator {
  // https://www.urbandictionary.com/define.php?term=lfg
  fun lfg(screen: ScreenKey)
}

interface ScreenKey {
  object Close : ScreenKey
}
