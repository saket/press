package press.sync.stats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import press.widgets.ThemeAwareActivity

class SyncStatsForNerdsActivity : ThemeAwareActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(SyncStatsForNerdsView(this))
  }

  companion object {
    fun intent(context: Context) = Intent(context, SyncStatsForNerdsActivity::class.java)
  }
}
