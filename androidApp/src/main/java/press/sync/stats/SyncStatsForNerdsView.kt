package press.sync.stats

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.core.view.setPadding
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.stats.SyncStatsForNerdsPresenter
import me.saket.press.shared.sync.stats.SyncStatsForNerdsUiModel
import me.saket.press.shared.theme.TextStyles.mainBody
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextView
import me.saket.press.shared.ui.models
import press.extensions.findParentOfType
import press.extensions.interceptPullToCollapseOnView
import press.extensions.textColor
import press.theme.themeAware
import press.widgets.PressToolbar

class SyncStatsForNerdsView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  private val presenter: SyncStatsForNerdsPresenter
) : ContourLayout(context) {

  private val toolbar = PressToolbar(context).apply {
    title = context.strings().sync.nerd_stats_title
  }

  private val directorySizeView = TextView(context, mainBody).apply {
    themeAware {
      textColor = it.textColorPrimary
    }
  }

  private val logsLabelView = TextView(context, mainBody).apply {
    text = context.strings().sync.nerd_stats_logs_label
    themeAware {
      textColor = it.textColorPrimary
    }
  }

  private val logsView = TextView(context, smallBody).apply {
    setTextIsSelectable(true)
    themeAware {
      textColor = it.textColorPrimary
    }
  }

  private val logsScrollView = ScrollView(context).apply {
    isFillViewport = true
    themeAware {
      setBackgroundColor(it.window.editorBackgroundColor)
    }

    addView(
      HorizontalScrollView(context).also {
        it.clipToPadding = false
        it.setPadding(22.dip)
        it.addView(logsView)
      }
    )
  }

  init {
    id = R.id.syncstats_view
    toolbar.layoutBy(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
    directorySizeView.layoutBy(
      x = matchParentX(marginLeft = 22.dip, marginRight = 22.dip),
      y = topTo { toolbar.bottom() + 8.ydip }
    )
    logsLabelView.layoutBy(
      x = matchXTo(directorySizeView),
      y = topTo { directorySizeView.bottom() + 8.ydip }
    )
    logsScrollView.layoutBy(
      x = matchParentX(),
      y = topTo { logsLabelView.bottom() + 16.ydip }.bottomTo { parent.bottom() }
    )
    contourHeightMatchParent()

    themeAware {
      setBackgroundColor(it.window.backgroundColor)
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    presenter.models()
      .takeUntil(detaches())
      .observeOn(mainThread())
      .subscribe(::render)

    val page = findParentOfType<ExpandablePageLayout>()
    page?.pullToCollapseInterceptor = interceptPullToCollapseOnView(logsScrollView)
  }

  private fun render(model: SyncStatsForNerdsUiModel) {
    directorySizeView.text = model.gitDirectorySize
    logsView.text = model.logs
  }
}
