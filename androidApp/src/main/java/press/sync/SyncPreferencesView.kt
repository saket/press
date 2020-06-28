package press.sync

import android.content.Context
import android.net.Uri
import androidx.appcompat.widget.AppCompatButton
import androidx.browser.customtabs.CustomTabsIntent
import com.jakewharton.rxbinding3.view.detaches
import com.squareup.contour.ContourLayout
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import me.saket.press.shared.localization.strings
import me.saket.press.shared.sync.SyncPreferencesEvent.AuthorizeClicked
import me.saket.press.shared.sync.SyncPreferencesPresenter
import me.saket.press.shared.sync.SyncPreferencesUiEffect
import me.saket.press.shared.sync.SyncPreferencesUiEffect.OpenAuthorizationUrl
import me.saket.press.shared.sync.SyncPreferencesUiModel
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import press.theme.themed
import press.util.exhaustive
import press.widgets.PressToolbar

class SyncPreferencesView @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted private val onDismiss: () -> Unit,
  private val presenter: SyncPreferencesPresenter
) : ContourLayout(context) {

  private val toolbar = themed(PressToolbar(context)).apply {
    title = context.strings().syncPreferences.title
    setNavigationOnClickListener { onDismiss() }
    applyLayout(
        x = matchParentX(),
        y = topTo { parent.top() }
    )
  }

  private val authorizeButton = AppCompatButton(context).apply {
    text = "Log into GitHub"
    applyLayout(
        x = centerHorizontallyTo { parent.centerX() },
        y = centerVerticallyTo { parent.centerY() }
    )
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    authorizeButton.setOnClickListener {
      presenter.dispatch(AuthorizeClicked)
    }

    presenter.uiUpdates()
        .takeUntil(detaches())
        .observeOn(mainThread())
        .subscribe(models = ::render, effects = ::render)
  }

  private fun render(model: SyncPreferencesUiModel) = Unit

  private fun render(effect: SyncPreferencesUiEffect) {
    when (effect) {
      is OpenAuthorizationUrl -> CustomTabsIntent.Builder()
          .addDefaultShareMenuItem()
          .build()
          .launchUrl(context, Uri.parse(effect.url))
    }.exhaustive
  }

  @AssistedInject.Factory
  interface Factory {
    fun create(context: Context, onDismiss: () -> Unit): SyncPreferencesView
  }
}
