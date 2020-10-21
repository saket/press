package press.sync.stats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import press.PressApp
import press.widgets.ThemeAwareActivity
import javax.inject.Inject

class SyncStatsForNerdsActivity : ThemeAwareActivity() {
  @Inject lateinit var viewFactory: SyncStatsForNerdsView.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    PressApp.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(viewFactory.create(this))
  }

  companion object {
    fun intent(context: Context) = Intent(context, SyncStatsForNerdsActivity::class.java)
  }
}
