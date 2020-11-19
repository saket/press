package me.saket.press.shared.ui

class FakeNavigator : Navigator {
  private val backstack = ArrayDeque<ScreenKey>()

  override fun lfg(screen: ScreenKey) {
    backstack.addFirst(screen)
  }

  override fun clearTopAndLfg(screen: ScreenKey) {
    backstack.clear()
    lfg(screen)
  }

  override fun goBack(otherwise: (() -> Unit)?) {
    if (backstack.isEmpty()) {
      otherwise?.invoke()
    } else {
      backstack.removeFirst()
    }
  }

  fun pop() = backstack.removeFirstOrNull()
}
