package me.saket.press.shared.ui

class FakeNavigator : Navigator {
  private val backstack = ArrayDeque<Any>()

  override fun lfg(screen: ScreenKey) {
    backstack.addFirst(screen)
  }

  override fun clearTopAndLfg(screen: ScreenKey) {
    backstack.clear()
    lfg(screen)
  }

  override fun goBack(result: ScreenResult?) {
    backstack.addFirst(Back(result))
  }

  fun pop(): Any? = backstack.removeFirstOrNull()
}

data class Back(val result: ScreenResult?)
