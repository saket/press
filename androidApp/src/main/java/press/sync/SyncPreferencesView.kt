package press.sync

import android.content.Context
import android.util.AttributeSet
import android.widget.ViewFlipper
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.inflation.InflationInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.R
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.SyncPreferencesEvent.DisableSyncClicked
import me.saket.press.shared.sync.SyncPreferencesEvent.SetupHostClicked
import me.saket.press.shared.sync.SyncPreferencesPresenter
import me.saket.press.shared.sync.SyncPreferencesUiModel
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncDisabled
import me.saket.press.shared.sync.SyncPreferencesUiModel.SyncEnabled
import me.saket.press.shared.ui.models
import press.extensions.setDisplayedChild
import press.extensions.unsafeLazy
import press.navigation.navigator
import press.theme.themeAware
import press.widgets.PressToolbar

class SyncPreferencesView @InflationInject constructor(
  @Assisted context: Context,
  @Assisted attrs: AttributeSet? = null,
  presenterFactory: SyncPreferencesPresenter.Factory
) : ContourLayout(context) {

  private val toolbar = PressToolbar(context).apply {
    title = context.strings().sync.title
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

  private val presenter by unsafeLazy {
    presenterFactory.create(
      SyncPreferencesPresenter.Args(navigator = navigator())
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

    presenter.models()
      .takeUntil(detaches())
      .observeOn(mainThread())
      .subscribe(::render)
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
}
