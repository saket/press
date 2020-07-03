package press.sync

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.core.view.isGone
import androidx.transition.TransitionManager
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.SyncPreferencesEvent.DisableSyncClicked
import me.saket.press.shared.sync.SyncPreferencesPresenter
import me.saket.press.shared.sync.SyncPreferencesUiModel
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncingDisabled
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncingEnabled
import me.saket.press.shared.theme.TextStyles
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import press.theme.themeAware
import press.theme.themed
import press.widgets.PressButton
import press.widgets.PressToolbar
import press.widgets.dp
import press.extensions.updateMargins

class SyncPreferencesView @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted private val onDismiss: () -> Unit,
  private val presenter: SyncPreferencesPresenter
) : ContourLayout(context) {

  private val toolbar = themed(PressToolbar(context)).apply {
    title = context.strings().sync.title
    setNavigationOnClickListener { onDismiss() }
    applyLayout(
        x = matchParentX(),
        y = topTo { parent.top() }
    )
  }

  private val syncingDisabledView = SyncingDisabledView(context)
  private val syncingEnabledView = SyncingEnabledView(context)

  init {
    themeAware {
      background = ColorDrawable(it.window.backgroundColor)
    }

    arrayOf(syncingEnabledView, syncingDisabledView).forEach {
      it.isGone = true
      it.applyLayout(
          x = matchParentX(marginLeft = 22.dip, marginRight = 22.dip),
          y = topTo { toolbar.bottom() + 8.ydip }.bottomTo { parent.bottom() }
      )
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    syncingEnabledView.disableButton.setOnClickListener {
      presenter.dispatch(DisableSyncClicked)
    }

    presenter.uiUpdates()
        .takeUntil(detaches())
        .observeOn(mainThread())
        .subscribe(models = ::render)
  }

  private fun render(model: SyncPreferencesUiModel) {
    TransitionManager.beginDelayedTransition(this)
    syncingEnabledView.isGone = model !is SyncingEnabled
    syncingDisabledView.isGone = model !is SyncingDisabled

    return when (model) {
      is SyncingDisabled -> syncingDisabledView.render(model)
      is SyncingEnabled -> syncingEnabledView.render(model)
    }
  }

  @AssistedInject.Factory
  interface Factory {
    fun create(context: Context, onDismiss: () -> Unit): SyncPreferencesView
  }
}

private class SyncingDisabledView(context: Context) : ContourLayout(context) {
  private val messageView = themed(TextView(context)).apply {
    text = context.strings().sync.sync_disabled_message
    TextStyles.Secondary.applyStyle(this)
    applyLayout(
        x = matchParentX(),
        y = topTo { parent.top() }
    )
  }

  private val gitHostButtons = LinearLayout(context).apply {
    orientation = VERTICAL
    applyLayout(
        x = matchParentX(),
        y = topTo { messageView.bottom() + 20.ydip }
    )
  }

  init {
    contourHeightOf { gitHostButtons.bottom() }
  }

  fun render(model: SyncingDisabled) {
    gitHostButtons.removeAllViews()
    model.availableGitHosts.forEach { host ->
      val button = themed(PressButton(context)).apply {
        text = context.strings().sync.setup_sync_with_host.format(host.displayName)
        setOnClickListener {
          context.startActivity(GitHostIntegrationActivity.intent(context, host))
        }
      }
      gitHostButtons.addView(button, WRAP_CONTENT, WRAP_CONTENT)
      button.updateMargins(bottom = dp(8))
    }
  }
}

private class SyncingEnabledView(context: Context) : ContourLayout(context) {
  private val setupInfoView = themed(TextView(context)).apply {
    TextStyles.Secondary.applyStyle(this)
    applyLayout(
        x = matchParentX(),
        y = topTo { parent.top() }
    )
  }

  private val statusView = themed(TextView(context)).apply {
    TextStyles.Secondary.applyStyle(this)
    applyLayout(
        x = matchParentX(),
        y = topTo { setupInfoView.bottom() + 16.ydip }
    )
  }

  val disableButton = themed(PressButton(context)).apply {
    text = "Disable sync on this device"
    applyLayout(
        x = leftTo { parent.left() },
        y = topTo { statusView.bottom() + 20.ydip }
    )
  }

  init {
    contourHeightOf { disableButton.bottom() }
  }

  fun render(model: SyncingEnabled) {
    setupInfoView.text = model.setupInfo
    statusView.text = model.status
  }
}
