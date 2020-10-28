package press.sync.stats

import android.content.Context
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.core.view.setPadding
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.stats.SyncStatsForNerdsPresenter
import me.saket.press.shared.sync.stats.SyncStatsForNerdsUiModel
import me.saket.press.shared.theme.TextStyles.mainBody
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextView
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import press.extensions.textColor
import press.findActivity
import press.theme.themeAware
import press.widgets.PressToolbar

class SyncStatsForNerdsView @AssistedInject constructor(
  @Assisted context: Context,
  private val presenter: SyncStatsForNerdsPresenter
) : ContourLayout(context) {

  private val toolbar = PressToolbar(context).apply {
    title = context.strings().sync.nerd_stats_title
    setNavigationOnClickListener { findActivity().finish() }
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

    presenter.uiUpdates()
      .takeUntil(detaches())
      .observeOn(mainThread())
      .subscribe(::render)
  }

  private fun render(model: SyncStatsForNerdsUiModel) {
    directorySizeView.text = model.gitDirectorySize
    logsView.text = model.logs
  }

  @AssistedInject.Factory
  interface Factory {
    fun create(context: Context): SyncStatsForNerdsView
  }
}
