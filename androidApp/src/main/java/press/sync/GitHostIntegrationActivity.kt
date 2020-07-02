package press.sync

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import me.saket.press.shared.DeepLink
import me.saket.press.shared.DeepLinks
import me.saket.press.shared.sync.git.GitHost
import press.PressApp
import press.widgets.ThemeAwareActivity
import javax.inject.Inject

class GitHostIntegrationActivity : ThemeAwareActivity() {

  @Inject lateinit var viewFactory: GitHostIntegrationView.Factory
  @Inject lateinit var deepLinks: DeepLinks

  override fun onCreate(savedInstanceState: Bundle?) {
    PressApp.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(viewFactory.create(this, host = readHost(intent), onDismiss = ::finish))

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
    fun readHost(intent: Intent) =
      intent.getSerializableExtra("host") as GitHost

    fun intent(context: Context, host: GitHost) =
      Intent(context, GitHostIntegrationActivity::class.java).apply {
        putExtra("host", host)
      }
  }
}
