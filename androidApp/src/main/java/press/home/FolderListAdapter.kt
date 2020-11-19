package press.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.reactivex.subjects.PublishSubject
import press.home.FolderListAdapter.FolderVH
import me.saket.press.shared.home.HomeUiModel.Folder as Model

class FolderListAdapter : ListAdapter<Model, FolderVH>(FolderDiffer()) {
  val clicks = PublishSubject.create<Model>()

  init {
    setHasStableIds(true)
    stateRestorationPolicy = PREVENT_WHEN_EMPTY
  }

  override fun getItemId(position: Int) =
    getItem(position).adapterId

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    FolderVH(
      FolderRowView(parent.context).apply {
        setOnClickListener { clicks.onNext(model) }
      }
    )

  override fun onBindViewHolder(holder: FolderVH, position: Int) {
    holder.view.render(getItem(position))
  }

  class FolderVH(val view: FolderRowView) : ViewHolder(view)
}

private class FolderDiffer : DiffUtil.ItemCallback<Model>() {
  override fun areItemsTheSame(oldItem: Model, newItem: Model) = oldItem.id == newItem.id
  override fun areContentsTheSame(oldItem: Model, newItem: Model) = oldItem == newItem
}
