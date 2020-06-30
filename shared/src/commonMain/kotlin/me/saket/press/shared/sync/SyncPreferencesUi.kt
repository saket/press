package me.saket.press.shared.sync

import me.saket.press.shared.sync.git.service.GitRepositoryInfo

interface GitHostAuthEvent {
  data class GitRepositoryClicked(val repo: GitRepositoryInfo) : GitHostAuthEvent
}

sealed class GitHostAuthUiModel {
  object Loading : GitHostAuthUiModel()
  class FullscreenError(val onRetry: () -> Unit) : GitHostAuthUiModel()
  data class SelectRepo(val repositories: List<GitRepositoryInfo>) : GitHostAuthUiModel()
}

sealed class GitHostAuthUiEffect {
  data class OpenAuthorizationUrl(val url: String) : GitHostAuthUiEffect()
}
