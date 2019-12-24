package press

import android.app.Activity
import android.os.Bundle
import press.editor.EditorActivity

class IntentReceiverActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    processIntent()

  }

  private fun processIntent() {

    val intent = EditorActivity.intent(this)
    startActivity(intent)
    finish()
  }
}