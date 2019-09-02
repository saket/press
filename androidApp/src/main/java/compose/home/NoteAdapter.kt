package compose.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import me.saket.compose.shared.note.Note

class NoteAdapter : ListAdapter<Note, NoteVH>(NoteDiffer()) {

  override fun getItemId(position: Int) =
    getItem(position).id

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    NoteVH(NoteRowView(parent.context))

  override fun onBindViewHolder(holder: NoteVH, position: Int) {
    holder.view.render(getItem(position))
  }
}

class NoteDiffer: DiffUtil.ItemCallback<Note>() {
  override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
  override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
}

class NoteVH(val view: NoteRowView) : ViewHolder(view)