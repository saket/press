package press.navigation

import android.content.Intent.FILL_IN_ACTION
import android.content.Intent.FILL_IN_DATA
import android.content.Intent.FILL_IN_SOURCE_BOUNDS
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Proxy activity for [TheActivity] that enforces [FLAG_ACTIVITY_SINGLE_TOP] at runtime.
 * This is used as a target for all intents originating from outside Press, e.g., deep-links
 * or intent sharing for saving a note.
 */
class IntentReceiverActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    startActivity(
      TheActivity.intent(this, singleTop = true).also {
        it.fillIn(intent, FILL_IN_ACTION or FILL_IN_DATA or FILL_IN_SOURCE_BOUNDS)
      }
    )
    finish()
  }
}
