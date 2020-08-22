package me.saket.press.shared.sync.git

import me.saket.press.shared.sync.git.service.GitRepositoryInfo

interface GitHostIntegrationEvent {
  data class GitRepositoryClicked(val repo: GitRepositoryInfo) : GitHostIntegrationEvent
  data class RetryClicked(val failure: FailureKind) : GitHostIntegrationEvent
  data class SearchTextChanged(val text: String) : GitHostIntegrationEvent
}

// todo: flatten and add 'screen title'.
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

data class HighlightedText(
  val text: String,
  val highlight: IntRange?
) {
  companion object {
    fun from(text: String, query: String): HighlightedText {
      return when (val queryIndex = text.indexOf(query, ignoreCase = true)) {
        -1 -> HighlightedText(text, null)
        else -> HighlightedText(text, queryIndex..(queryIndex + query.length))
      }
    }
  }
}

sealed class FailureKind {
  object Authorization : FailureKind()
  object FetchingRepos : FailureKind()
  object AddingDeployKey : FailureKind()
}
