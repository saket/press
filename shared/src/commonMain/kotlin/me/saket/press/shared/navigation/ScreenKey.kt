package me.saket.press.shared.navigation

sealed class ScreenKey {
  object ComposeNewNote : ScreenKey()
  object Back : ScreenKey()
}