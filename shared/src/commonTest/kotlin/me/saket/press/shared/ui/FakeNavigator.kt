package me.saket.press.shared.ui

class FakeNavigator : Navigator {
  private val backstack = ArrayDeque<ScreenKey>()

  override fun lfg(screen: ScreenKey) {
    backstack.addFirst(screen)
  }

  override fun goBack(): Boolean {
    return if (backstack.isEmpty()) {
      false
    } else {
      backstack.removeFirst()
      true
    }
  }

  fun pop() = backstack.removeFirstOrNull()
}
