package press.sync

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import me.saket.press.shared.DeepLink
import me.saket.press.shared.DeepLinks
import press.App
import press.widgets.ThemeAwareActivity
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class PreferencesActivity : ThemeAwareActivity() {

  @Inject lateinit var viewFactory: SyncPreferencesView.Factory
  @Inject lateinit var deepLinks: DeepLinks

  override fun onCreate(savedInstanceState: Bundle?) {
    App.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(viewFactory.create(this, onDismiss = ::finish))
  }

  companion object {
    fun intent(context: Context) = Intent(context, PreferencesActivity::class.java)
  }
}
