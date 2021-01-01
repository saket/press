package me.saket.press.shared.preferences.sync.setup

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.syncer.git.GitHost
import me.saket.press.shared.syncer.git.service.GitRepositoryInfo
import me.saket.press.shared.ui.ScreenKey
import me.saket.press.shared.ui.ScreenResult

@AndroidParcelize
data class NewGitRepositoryScreenKey(
  val username: String,
  val gitHost: GitHost,
  val preFilledRepoName: String
) : ScreenKey

interface NewGitRepositoryEvent {
  data class NameTextChanged(val name: String) : NewGitRepositoryEvent
  object SubmitClicked : NewGitRepositoryEvent
}

data class NewGitRepositoryUiModel(
  val repoUrlPreview: String?,
  val errorMessage: String?,
  val submitEnabled: Boolean,
  val isLoading: Boolean,
)

data class NewGitRepositoryCreatedResult(
  val repo: GitRepositoryInfo
) : ScreenResult
