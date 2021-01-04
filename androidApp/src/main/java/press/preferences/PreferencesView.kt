package press.preferences

import android.content.Context
import android.graphics.Color.BLACK
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.MotionEvent
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.contour.ContourLayout
import me.saket.inboxrecyclerview.ExpandedItemFinder
import me.saket.inboxrecyclerview.InboxRecyclerView
import me.saket.inboxrecyclerview.dimming.DimPainter
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.preferences.PreferenceCategory.AboutApp
import me.saket.press.shared.preferences.PreferenceCategory.LookAndFeel
import me.saket.press.shared.preferences.PreferenceCategory.Sync
import me.saket.press.shared.preferences.PreferenceCategoryItemModel
import me.saket.press.shared.preferences.PreferenceCategoryScreenKey
import press.extensions.findParentOfType
import press.extensions.interceptPullToCollapseOnView
import press.navigation.navigator
import press.navigation.transitions.ExpandableScreenHost
import press.theme.themeAware
import press.widgets.DividerItemDecoration
import press.widgets.PressToolbar
import press.widgets.SlideDownItemAnimator

class PreferencesView(context: Context) : ContourLayout(context), ExpandableScreenHost {
  private val toolbar = PressToolbar(context).apply {
    title = context.strings().prefs.screen_title
  }

  private val categoryList = InboxRecyclerView(context).apply {
    layoutManager = LinearLayoutManager(context)
    dimPainter = DimPainter.listAndPage(color = BLACK, alpha = 0.25f)
    itemAnimator = SlideDownItemAnimator()
    addItemDecoration(DividerItemDecoration())
  }

  private val categoryAdapter = PreferenceCategoryListAdapter(
    categories = preferenceCategories(),
    onClick = { item ->
      navigator().lfg(PreferenceCategoryScreenKey(item.category))
    }
  )

  init {
    id = R.id.preferences_view
    themeAware {
      setBackgroundColor(it.window.backgroundColor)
    }

    toolbar.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
    categoryList.layoutBy(
      x = matchParentX(),
      y = matchParentY()
    )

    categoryList.adapter = categoryAdapter

    // Draw InboxRecyclerView's dimming over toolbar.
    toolbar.doOnLayout {
      categoryList.clipToPadding = true
      categoryList.updatePadding(top = toolbar.bottom)
    }
  }

  private fun preferenceCategories(): List<PreferenceCategoryItemModel> {
    val strings = context.strings().prefs
    return listOf(
      PreferenceCategoryItemModel(
        title = strings.category_title_look_and_feel,
        subtitle = strings.category_subtitle_look_and_feel,
        category = LookAndFeel
      ),
      PreferenceCategoryItemModel(
        title = strings.category_title_sync,
        subtitle = strings.category_subtitle_sync,
        category = Sync
      ),
      PreferenceCategoryItemModel(
        title = strings.category_title_about_app,
        subtitle = Html.fromHtml(strings.category_subtitle_about_app, FROM_HTML_MODE_LEGACY),
        category = AboutApp
      )
    )
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    val page = findParentOfType<ExpandablePageLayout>()
    page?.pullToCollapseInterceptor = interceptPullToCollapseOnView(categoryList)
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    // The note list is positioned in front of the toolbar so that its items can go
    // over it, but RV steals all touch events even if there isn't a child under to
    // receive the event.
    return if (ev.y > toolbar.y && ev.y < (toolbar.y + toolbar.height)) {
      toolbar.dispatchTouchEvent(ev)
    } else {
      super.dispatchTouchEvent(ev)
    }
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
