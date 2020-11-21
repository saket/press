package press.home

import androidx.core.view.children
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.reactivex.subjects.PublishSubject
import me.saket.inboxrecyclerview.ExpandedItemFinder.FindResult
import me.saket.press.shared.home.HomeUiModel.Folder
import me.saket.press.shared.home.HomeUiModel.Row

abstract class BaseHomeRowAdapter<T : Row, VH : ViewHolder> :
  ListAdapter<Row, VH>(RowDiffer()) {
  val clicks = PublishSubject.create<T>()

  init {
    stateRestorationPolicy = PREVENT_WHEN_EMPTY
  }

  @Suppress("UNCHECKED_CAST")
  override fun getItem(position: Int): T {
    return super.getItem(position) as T
  }

  protected inline fun <reified V : ViewHolder> findExpandedItem(
    parent: RecyclerView,
    crossinline predicate: (V) -> Boolean
  ): FindResult? {
    return parent.children.map(parent::getChildViewHolder)
      .filterIsInstance(V::class.java)
      .filter { predicate(it) }
      .firstOrNull()
      ?.let {
        FindResult(
          itemAdapterPosition = it.absoluteAdapterPosition,
          itemView = it.itemView
        )
      }
  }
}

private class RowDiffer : DiffUtil.ItemCallback<Row>() {
  override fun areItemsTheSame(oldItem: Row, newItem: Row) = oldItem.id == newItem.id
  override fun areContentsTheSame(oldItem: Row, newItem: Row) = oldItem == newItem
}
