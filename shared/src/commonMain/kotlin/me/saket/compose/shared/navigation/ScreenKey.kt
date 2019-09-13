package me.saket.compose.shared.navigation

sealed class ScreenKey {
  object ComposeNewNote : ScreenKey()
  object Back : ScreenKey()
}