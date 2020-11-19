package press.sync

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.ViewFlipper
import androidx.browser.customtabs.CustomTabsIntent
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.inject.inflation.InflationInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.SyncPreferencesEvent.DisableSyncClicked
import me.saket.press.shared.sync.SyncPreferencesEvent.SetupHostClicked
import me.saket.press.shared.sync.SyncPreferencesPresenter
import me.saket.press.shared.sync.SyncPreferencesUiEffect
import me.saket.press.shared.sync.SyncPreferencesUiEffect.OpenUrl
import me.saket.press.shared.sync.SyncPreferencesUiModel
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncDisabled
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncEnabled
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import press.extensions.setDisplayedChild
import press.navigation.navigator
import press.theme.themeAware
import press.widgets.PressToolbar

class SyncPreferencesView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  private val presenter: SyncPreferencesPresenter
) : ContourLayout(context) {

  private val toolbar = PressToolbar(context).apply {
    title = context.strings().sync.title
    setNavigationOnClickListener { navigator().goBack() }
    applyLayout(
      x = matchParentX(),
      y = topTo { parent.top() }
    )
  }

  private val syncDisabledView = SyncDisabledView(context)
  private val syncEnabledView = SyncEnabledView(context)

  private val contentFlipperView = ViewFlipper(context).apply {
    animateFirstView = false
    setInAnimation(context, R.anim.slide_and_fade_in_from_bottom)
    setOutAnimation(context, R.anim.slide_and_fade_out_to_top)
    addView(syncDisabledView)
    addView(syncEnabledView)
    applyLayout(
      x = matchParentX(),
      y = topTo { toolbar.bottom() }.bottomTo { parent.bottom() }
    )
  }

  init {
    id = R.id.syncpreferences_view
    themeAware {
      setBackgroundColor(it.window.backgroundColor)
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    syncEnabledView.onDisableClick = {
      presenter.dispatch(DisableSyncClicked)
    }

    presenter.uiUpdates()
      .takeUntil(detaches())
      .observeOn(mainThread())
      .subscribe(models = ::render, effects = ::render)
  }

  private fun render(model: SyncPreferencesUiModel) {
    return when (model) {
      is SyncDisabled -> {
        contentFlipperView.setDisplayedChild(syncDisabledView)
        syncDisabledView.render(model, onClick = { host ->
          presenter.dispatch(SetupHostClicked(host))
        })
      }
      is SyncEnabled -> {
        contentFlipperView.setDisplayedChild(syncEnabledView)
        syncEnabledView.render(model)
      }
    }
  }

  private fun render(effect: SyncPreferencesUiEffect) {
    return when (effect) {
      is OpenUrl -> CustomTabsIntent.Builder()
        .setShowTitle(true)
        .addDefaultShareMenuItem()
        .build()
        .launchUrl(context, Uri.parse(effect.url))
    }
  }
}
