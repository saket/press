package me.saket.press.shared.sync

import me.saket.press.shared.sync.git.GitHost

interface SyncPreferencesEvent {
  object DisableSyncClicked : SyncPreferencesEvent
}

sealed class SyncPreferencesUiModel {
  data class SyncingDisabled(
    val availableGitHosts: List<GitHost>
  ) : SyncPreferencesUiModel()

  data class SyncingEnabled(
    val setupInfo: String,
    val status: String
  ) : SyncPreferencesUiModel()
}
