package press.preferences

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.saket.inboxrecyclerview.expander.InboxItemExpander
import me.saket.press.shared.preferences.PreferenceCategoryItemModel
import me.saket.press.shared.preferences.PreferenceCategoryScreenKey
import me.saket.press.shared.ui.ScreenKey
import press.preferences.PreferenceCategoryListAdapter.CategoryVH

class PreferenceCategoryListAdapter(
  private val categories: List<PreferenceCategoryItemModel>,
  private val onClick: (PreferenceCategoryItemModel) -> Unit
) : RecyclerView.Adapter<CategoryVH>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
    return CategoryVH(PreferenceCategoryRowView(parent.context).apply {
      setOnClickListener {
        onClick(model)
      }
    })
  }

  override fun onBindViewHolder(holder: CategoryVH, position: Int) {
    holder.view.render(categories[position])
  }

  override fun getItemCount(): Int {
    return categories.size
  }

  fun createScreenExpander(): InboxItemExpander<ScreenKey> {
    return InboxItemExpander { screen, viewHolders ->
      if (screen is PreferenceCategoryScreenKey) {
        viewHolders.firstOrNull { it is CategoryVH && it.view.model.category == screen.category }
      } else {
        null
      }
    }
  }

  class CategoryVH(val view: PreferenceCategoryRowView) : RecyclerView.ViewHolder(view)
}
