package press.sync

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import me.saket.press.shared.DeepLink
import me.saket.press.shared.DeepLinks
import press.PressApp
import press.widgets.ThemeAwareActivity
import javax.inject.Inject

class GitHostIntegrationActivity : ThemeAwareActivity() {

  @Inject lateinit var viewFactory: GitHostIntegrationView.Factory
  @Inject lateinit var deepLinks: DeepLinks

  override fun onCreate(savedInstanceState: Bundle?) {
    PressApp.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(viewFactory.create(this, onDismiss = ::finish))

    // The DeepLink will usually be dispatched through onNewIntent(),
    // but it's possible that Press gets killed while in background.
    maybeReadDeepLink(intent)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    maybeReadDeepLink(intent)
  }

  private fun maybeReadDeepLink(intent: Intent) {
    if (intent.action == ACTION_VIEW && intent.data != null) {
      deepLinks.broadcast(DeepLink(intent.dataString!!))
    }
  }

  companion object {
    fun intent(context: Context) = Intent(context, GitHostIntegrationActivity::class.java)
  }
}
