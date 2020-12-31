package press.preferences

import android.content.Context
import android.graphics.Color.BLACK
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.inboxrecyclerview.ExpandedItemFinder
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.dimming.DimPainter
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.preferences.PreferenceCategoryScreenKey
import me.saket.press.shared.preferences.PreferencesPresenter
import me.saket.press.shared.ui.models
import press.extensions.findParentOfType
import press.extensions.interceptPullToCollapseOnView
import press.navigation.navigator
import press.navigation.transitions.ExpandableScreenHost
import press.widgets.DividerItemDecoration
import press.widgets.PressToolbar
import press.widgets.SlideDownItemAnimator

class PreferencesView @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
) : ContourLayout(context), ExpandableScreenHost {

  private val toolbar = PressToolbar(context).apply {
    title = context.strings().prefs.screen_title
  }

  private val categoryList = InboxRecyclerView(context).apply {
    layoutManager = LinearLayoutManager(context)
    dimPainter = DimPainter.listAndPage(color = BLACK, alpha = 0.25f)
    itemAnimator = SlideDownItemAnimator()
    addItemDecoration(DividerItemDecoration())
  }
  lateinit var categoryAdapter: PreferenceCategoryListAdapter

  init {
    id = R.id.preferences_view

    toolbar.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
    categoryList.layoutBy(
      x = matchParentX(),
      y = topTo { toolbar.top() }.bottomTo { parent.bottom() }
    )
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    PreferencesPresenter(context.strings()).models()
      .take(1)
      .takeUntil(detaches())
      .observeOn(mainThread())
      .subscribe { model ->
        categoryAdapter = PreferenceCategoryListAdapter(
          categories = model.categories,
          onClick = { item ->
            navigator().lfg(item.screenKey)
          }
        )
        categoryList.adapter = categoryAdapter
      }

    val page = findParentOfType<ExpandablePageLayout>()
    page?.pullToCollapseInterceptor = interceptPullToCollapseOnView(categoryList)
  }

  override fun identifyExpandingItem(): ExpandedItemFinder {
    return ExpandedItemFinder { parent, id ->
      if (id is PreferenceCategoryScreenKey) {
        categoryAdapter.findExpandedItem(parent, id.category)
      } else {
        null
      }
    }
  }
}
