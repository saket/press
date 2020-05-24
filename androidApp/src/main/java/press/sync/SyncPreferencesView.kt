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
import me.saket.press.shared.sync.SyncPreferencesEvent.AuthorizeClicked
import me.saket.press.shared.sync.SyncPreferencesEvent.AuthorizationGranted
import me.saket.press.shared.sync.SyncPreferencesPresenter
import me.saket.press.shared.sync.SyncPreferencesUiEffect
import me.saket.press.shared.sync.SyncPreferencesUiEffect.OpenAuthorizationUrl
import me.saket.press.shared.sync.SyncPreferencesUiModel
import me.saket.press.shared.ui.subscribe
import me.saket.press.shared.ui.uiUpdates
import press.util.exhaustive

class SyncPreferencesView @AssistedInject constructor(
  @Assisted context: Context,
  private val presenter: SyncPreferencesPresenter
) : ContourLayout(context) {

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

  fun handleDeepLink(url: String) {
    require(url.startsWith("intent://press/authorization-granted"))
    presenter.dispatch(AuthorizationGranted(url))
  }

  @AssistedInject.Factory
  interface Factory {
    fun create(context: Context): SyncPreferencesView
  }
}
