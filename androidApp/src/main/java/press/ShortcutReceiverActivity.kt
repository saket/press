package press

import android.content.Intent
import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import press.editor.EditorActivity

class ShortcutReceiverActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val preFilledNote = when (intent.action) {
      Intent.ACTION_SEND -> intent.getStringExtra(EXTRA_TEXT)
      else -> null
    }
    startActivity(EditorActivity.intent(this, preFilledNote))
    finish()
  }
}
