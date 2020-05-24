package me.saket.press.shared.sync

interface SyncPreferencesEvent {
  object AuthorizeClicked : SyncPreferencesEvent
  data class AuthorizationGranted(val callbackUrl: String) : SyncPreferencesEvent
}

class SyncPreferencesUiModel

sealed class SyncPreferencesUiEffect {
  data class OpenAuthorizationUrl(val url: String) : SyncPreferencesUiEffect()
}
