package me.saket.compose.shared.navigation

interface Navigator {
  fun goTo(screenKey: ScreenKey)
}