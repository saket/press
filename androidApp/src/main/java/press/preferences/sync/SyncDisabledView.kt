package press.preferences.sync

import android.content.Context
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import com.squareup.contour.ContourLayout
import me.saket.press.shared.localization.strings
import me.saket.press.shared.preferences.sync.SyncPreferencesUiModel.SyncDisabled
import me.saket.press.shared.syncer.git.GitHost
import me.saket.press.shared.theme.TextStyles.smallBody
import me.saket.press.shared.theme.TextView
import press.extensions.textColor
import press.extensions.updateMargins
import press.theme.themeAware
import press.widgets.PressButton
import press.widgets.dp

class SyncDisabledView(context: Context) : ContourLayout(context) {
  private val messageView = TextView(context, smallBody).apply {
    text = context.strings().sync.sync_disabled_message
    themeAware { textColor = it.textColorPrimary }
    applyLayout(
      x = matchParentX(marginLeft = 22.dip, marginRight = 22.dip),
      y = topTo { parent.top() + 8.ydip }
    )
  }

  private val gitHostButtons = LinearLayout(context).apply {
    orientation = VERTICAL
    applyLayout(
      x = matchXTo(messageView),
      y = topTo { messageView.bottom() + 20.ydip }
    )
  }

  init {
    contourHeightOf { gitHostButtons.bottom() }
  }

  fun render(model: SyncDisabled, onClick: (GitHost) -> Unit) {
    gitHostButtons.removeAllViews()
    model.availableGitHosts.forEach { host ->
      gitHostButtons.addView(createGitHostButton(host, onClick))
    }
  }

  private fun createGitHostButton(host: GitHost, onClick: (GitHost) -> Unit): Button {
    return PressButton(context, smallBody).also {
      it.text = context.strings().sync.setup_sync_with_host.format(host.displayName())
      it.layoutParams = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
      it.updateMargins(bottom = dp(8))
      it.themeAware { palette -> it.textColor = palette.textColorPrimary }
      it.setOnClickListener { onClick(host) }
    }
  }
}
