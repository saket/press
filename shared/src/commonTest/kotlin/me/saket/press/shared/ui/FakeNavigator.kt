package me.saket.press.shared.ui

class FakeNavigator : Navigator {
  private val backstack = ArrayDeque<ScreenKey>()

  override fun lfg(screen: ScreenKey) {
    backstack.addFirst(screen)
  }

  override fun goBack() {
    backstack.removeFirst()
  }

  fun pop() = backstack.removeFirstOrNull()
}
