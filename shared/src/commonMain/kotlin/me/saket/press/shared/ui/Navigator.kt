package me.saket.press.shared.ui

interface Navigator {
  /** https://www.urbandictionary.com/define.php?term=lfg */
  fun lfg(screen: ScreenKey)
}

interface ScreenKey {
  object Close : ScreenKey
}

@Suppress("FunctionName")
fun Navigator(navigate: (screen: ScreenKey) -> Unit): Navigator {
  return object : Navigator {
    override fun lfg(screen: ScreenKey) {
      navigate(screen)
    }
  }
}
