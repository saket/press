package press.sync

import android.os.Bundle
import press.PressApp
import press.widgets.ThemeAwareActivity
import javax.inject.Inject

class GitHostIntegrationActivity : ThemeAwareActivity() {
  @Inject lateinit var viewFactory: GitHostIntegrationView.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    PressApp.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(
        viewFactory.create(
            context = this,
            deepLink = intent.dataString!!,
            onDismiss = ::finish
        )
    )
  }
}
