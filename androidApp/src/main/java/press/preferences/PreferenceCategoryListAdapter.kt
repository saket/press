package press.preferences

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.saket.inboxrecyclerview.ExpandedItemFinder.FindResult
import me.saket.press.shared.preferences.PreferenceCategory
import me.saket.press.shared.preferences.PreferenceCategoryItemModel
import press.extensions.findExpandedItem
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

  fun findExpandedItem(parent: RecyclerView, category: PreferenceCategory): FindResult? {
    return findExpandedItem<CategoryVH>(parent) { holder ->
      holder.view.model.category == category
    }
  }

  class CategoryVH(val view: PreferenceCategoryRowView) : RecyclerView.ViewHolder(view)
}
