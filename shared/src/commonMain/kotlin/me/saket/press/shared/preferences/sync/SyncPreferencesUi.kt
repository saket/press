package me.saket.press.shared.preferences.sync

import me.saket.press.shared.syncer.git.GitHost

interface SyncPreferencesEvent {
  data class SetupHostClicked(val host: GitHost) : SyncPreferencesEvent
  object DisableSyncClicked : SyncPreferencesEvent
}

sealed class SyncPreferencesUiModel {
  data class SyncDisabled(
    val availableGitHosts: List<GitHost>
  ) : SyncPreferencesUiModel()

  data class SyncEnabled(
    val gitHost: GitHost,
    val remoteName: String,
    val remoteUrl: String,
    val status: String
  ) : SyncPreferencesUiModel()
}
