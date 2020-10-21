package press.sync.stats

import android.content.Context
import androidx.core.view.updatePaddingRelative
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.stats.SyncStatsForNerdsPresenter
import me.saket.press.shared.sync.stats.SyncStatsForNerdsUiModel
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

  private val directorySizeView = TextView(context, smallBody).apply {
    updatePaddingRelative(start = 16.dip, end = 16.dip)
    themeAware {
      textColor = it.textColorSecondary
    }
  }

  init {
    toolbar.layoutBy(
        x = matchParentX(),
        y = topTo { parent.top() }
    )
    directorySizeView.layoutBy(
        x = matchParentX(),
        y = topTo { toolbar.bottom() + 8.ydip }
    )

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
  }

  @AssistedInject.Factory
  interface Factory {
    fun create(context: Context): SyncStatsForNerdsView
  }
}
