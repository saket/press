package press.home

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.reactivex.subjects.PublishSubject
import me.saket.press.shared.home.HomeUiModel.Row

abstract class BaseHomeRowAdapter<T : Row, VH : ViewHolder> :
  ListAdapter<T, VH>(RowDiffer<T>()) {
  val clicks = PublishSubject.create<T>()

  init {
    stateRestorationPolicy = PREVENT_WHEN_EMPTY
  }
}

private class RowDiffer<T : Row> : DiffUtil.ItemCallback<T>() {
  override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem.id == newItem.id
  override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
}
