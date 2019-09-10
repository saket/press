package me.saket.compose.shared.navigation

actual class RealNavigator(private val goTo: (ScreenKey) -> Unit) : Navigator {

  override fun goTo(screenKey: ScreenKey) {
    goTo.invoke(screenKey)
  }
}