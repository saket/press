package press.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import me.saket.inboxrecyclerview.ExpandedItemFinder.FindResult
import me.saket.press.shared.db.FolderId
import press.home.FolderListAdapter.FolderVH
import me.saket.press.shared.home.HomeUiModel.Folder as Model

class FolderListAdapter : BaseHomeRowAdapter<Model, FolderVH>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    FolderVH(
      FolderRowView(parent.context).apply {
        setOnClickListener { clicks.onNext(model) }
      }
    )

  override fun onBindViewHolder(holder: FolderVH, position: Int) {
    holder.view.render(getItem(position))
  }

  fun findExpandedItem(parent: RecyclerView, folderId: FolderId): FindResult? {
    return findExpandedItem<FolderVH>(parent) { holder ->
      holder.view.model.id == folderId
    }
  }

  class FolderVH(val view: FolderRowView) : ViewHolder(view)
}
