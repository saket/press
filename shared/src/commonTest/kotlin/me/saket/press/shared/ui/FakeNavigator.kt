package me.saket.press.shared.ui

import me.saket.press.shared.IsoStack

class FakeNavigator : Navigator {
  private val backstack = IsoStack<Any>()
  private val intentLauncher = FakeIntentLauncher()

  override fun lfg(screen: ScreenKey) {
    backstack.push(screen)
  }

  override fun clearTopAndLfg(screen: ScreenKey) {
    backstack.clear()
    lfg(screen)
  }

  override fun splitScreenAndLfg(screen: ScreenKey) {
    backstack.push(screen)
  }

  override fun goBack(result: ScreenResult?) {
    backstack.push(Back(result))
  }

  fun pop(): Any? {
    return when {
      backstack.isEmpty() -> null
      else -> backstack.pop()
    }
  }

  override fun intentLauncher() = intentLauncher
}

data class Back(val result: ScreenResult?)
