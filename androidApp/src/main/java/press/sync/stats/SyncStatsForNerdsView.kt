package press.sync.stats

import android.content.Context
import com.squareup.contour.ContourLayout
import me.saket.press.shared.localization.strings
import press.findActivity
import press.widgets.PressToolbar

class SyncStatsForNerdsView(context: Context) : ContourLayout(context) {
  private val toolbar = PressToolbar(context).apply {
    title = context.strings().sync.nerd_stats_title
    setNavigationOnClickListener { findActivity().finish() }
  }

  init {
    toolbar.layoutBy(
        x = matchParentX(),
        y = topTo { parent.top() }
    )
  }
}
