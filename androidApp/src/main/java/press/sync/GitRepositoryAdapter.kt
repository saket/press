package press.sync

import android.view.ViewGroup
import androidx.core.view.isGone
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
    with(holder.view) {
      nameView.text = getItem(position).name
      dividerView.isGone = position == (itemCount - 1)
      setOnClickListener {
        onClick(getItem(position))
      }
    }
  }
}

class RepoViewHolder(val view: GitRepoItemView) : RecyclerView.ViewHolder(view)

private object ItemDiffer : DiffUtil.ItemCallback<GitRepositoryInfo>() {
  override fun areItemsTheSame(oldItem: GitRepositoryInfo, newItem: GitRepositoryInfo) =
    oldItem.name == newItem.name

  override fun areContentsTheSame(oldItem: GitRepositoryInfo, newItem: GitRepositoryInfo) =
    oldItem == newItem
}
