package me.saket.compose.shared.navigation

sealed class ScreenKey {
  object NewNote : ScreenKey()
  object Back : ScreenKey()
}