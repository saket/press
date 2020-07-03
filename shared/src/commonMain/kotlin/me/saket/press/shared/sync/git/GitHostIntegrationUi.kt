package me.saket.press.shared.sync.git

import me.saket.press.shared.sync.git.service.GitRepositoryInfo

interface GitHostIntegrationEvent {
  data class GitRepositoryClicked(val repo: GitRepositoryInfo) : GitHostIntegrationEvent
  data class RetryClicked(val failure: FailureKind) : GitHostIntegrationEvent
}

// todo: flatten and add 'screen title'.
sealed class GitHostIntegrationUiModel {
  object ShowProgress : GitHostIntegrationUiModel()
  data class ShowFailure(val kind: FailureKind) : GitHostIntegrationUiModel()
  data class SelectRepo(val repositories: List<GitRepositoryInfo>) : GitHostIntegrationUiModel()
}

sealed class FailureKind {
  object Authorization : FailureKind()
  object FetchingRepos : FailureKind()
  object AddingDeployKey : FailureKind()
}
