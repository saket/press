package press.sync

import android.content.Context
import android.content.Intent
import android.os.Bundle
import me.saket.press.shared.DeepLinks
import press.PressApp
import press.widgets.ThemeAwareActivity
import javax.inject.Inject

class PreferencesActivity : ThemeAwareActivity() {

  @Inject lateinit var viewFactory: SyncPreferencesView.Factory
  @Inject lateinit var deepLinks: DeepLinks

  override fun onCreate(savedInstanceState: Bundle?) {
    PressApp.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(viewFactory.create(this, onDismiss = ::finish))
  }

  companion object {
    fun intent(context: Context) = Intent(context, PreferencesActivity::class.java)
  }
}
