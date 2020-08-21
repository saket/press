package press.sync

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.saket.press.shared.sync.git.service.GitRepositoryInfo

class GitRepositoryAdapter : ListAdapter<GitRepositoryInfo, RepoViewHolder>(ItemDiffer) {
  lateinit var onClick: (GitRepositoryInfo) -> Unit

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
    return RepoViewHolder(GitRepoItemView(parent.context))
  }

  override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
    val repo = getItem(position)
    holder.view.render(repo, showDivider = position < itemCount - 1)
    holder.view.setOnClickListener { onClick(repo) }
  }
}

class RepoViewHolder(val view: GitRepoItemView) : RecyclerView.ViewHolder(view)

private object ItemDiffer : DiffUtil.ItemCallback<GitRepositoryInfo>() {
  override fun areItemsTheSame(oldItem: GitRepositoryInfo, newItem: GitRepositoryInfo) =
    oldItem.ownerAndName == newItem.ownerAndName

  override fun areContentsTheSame(oldItem: GitRepositoryInfo, newItem: GitRepositoryInfo) =
    oldItem == newItem
}
