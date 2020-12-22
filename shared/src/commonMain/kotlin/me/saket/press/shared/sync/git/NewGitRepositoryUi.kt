package me.saket.press.shared.sync.git

import me.saket.kgit.GitIdentity
import me.saket.press.shared.AndroidParcelize
import me.saket.press.shared.ui.ScreenKey

@AndroidParcelize
data class NewGitRepositoryScreenKey(
  val user: GitIdentity,
  val gitHost: GitHost
) : ScreenKey

interface NewGitRepositoryEvent {
  data class NameTextChanged(val name: String) : NewGitRepositoryEvent
  data class SubmitClicked(val privateRepo: Boolean) : NewGitRepositoryEvent
}

data class NewGitRepositoryUiModel(
  val repoUrlPreview: String,
  val errorMessage: String?,
  val isLoading: Boolean
)
