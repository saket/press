package me.saket.press.shared.sync

import me.saket.press.shared.sync.git.service.GitRepositoryInfo

interface GitHostAuthEvent {
  data class GitRepositoryClicked(val repo: GitRepositoryInfo) : GitHostAuthEvent
  data class RetryClicked(val failure: FailureKind) : GitHostAuthEvent
}

sealed class GitHostAuthUiModel {
  object ShowProgress : GitHostAuthUiModel()
  data class ShowFailure(val kind: FailureKind) : GitHostAuthUiModel()
  data class SelectRepo(val repositories: List<GitRepositoryInfo>) : GitHostAuthUiModel()
}

sealed class FailureKind {
  object Authorization : FailureKind()
  object FetchingRepos : FailureKind()
  object AddingDeployKey : FailureKind()
}

sealed class GitHostAuthUiEffect {
  data class OpenAuthorizationUrl(val url: String) : GitHostAuthUiEffect()
}
