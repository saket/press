package press.preferences.sync.setup

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import me.saket.press.shared.preferences.sync.setup.RepoUiModel
import me.saket.press.shared.syncer.git.service.GitRepositoryInfo

class GitRepositoryAdapter : ListAdapter<RepoUiModel, RepoViewHolder>(ItemDiffer) {
  lateinit var onClick: (GitRepositoryInfo) -> Unit

  init {
    stateRestorationPolicy = PREVENT_WHEN_EMPTY
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
    return RepoViewHolder(GitRepoRowView(parent.context))
  }

  override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
    val repo = getItem(position)
    holder.view.render(repo)
    holder.view.setOnClickListener { onClick(repo.repo) }
  }
}

class RepoViewHolder(val view: GitRepoRowView) : RecyclerView.ViewHolder(view)

private object ItemDiffer : DiffUtil.ItemCallback<RepoUiModel>() {
  override fun areItemsTheSame(oldItem: RepoUiModel, newItem: RepoUiModel) =
    oldItem.id == newItem.id

  override fun areContentsTheSame(oldItem: RepoUiModel, newItem: RepoUiModel) =
    oldItem == newItem
}
