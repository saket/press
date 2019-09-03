package me.saket.compose.shared.navigation

class FakeNavigator : Navigator {

  val backstack = mutableListOf<ScreenKey>()

  override fun goTo(screenKey: ScreenKey) {
    backstack.add(screenKey)
  }
}