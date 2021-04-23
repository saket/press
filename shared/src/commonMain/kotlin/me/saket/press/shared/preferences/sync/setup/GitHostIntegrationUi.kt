package me.saket.press.shared.preferences.sync.setup

import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.syncer.git.service.GitRepositoryInfo
import me.saket.press.shared.ui.HighlightedText
import me.saket.press.shared.ui.ScreenKey

@AndroidParcelize
data class GitHostIntegrationScreenKey(val deepLink: String) : ScreenKey

interface GitHostIntegrationEvent {
  data class GitRepositoryClicked(val repo: GitRepositoryInfo) : GitHostIntegrationEvent
  data class RetryClicked(val failure: FailureKind) : GitHostIntegrationEvent
  data class SearchTextChanged(val text: String) : GitHostIntegrationEvent
  object CreateNewGitRepoClicked : GitHostIntegrationEvent
}

sealed class GitHostIntegrationUiModel {
  object ShowProgress : GitHostIntegrationUiModel()
  data class ShowFailure(val kind: FailureKind) : GitHostIntegrationUiModel()
  data class SelectRepo(val repositories: List<RepoUiModel>) : GitHostIntegrationUiModel()
}

data class RepoUiModel(
  val id: Any,
  val owner: HighlightedText,
  val name: HighlightedText,
  val repo: GitRepositoryInfo
)

sealed class FailureKind {
  object Authorization : FailureKind()
  object FetchingRepos : FailureKind()
  object AddingDeployKey : FailureKind()
}
