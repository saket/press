package press

import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import press.editor.EditorActivity

class ShortcutReceiverActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Text received from android.intent.action.SEND
    val preFilledNote = intent.getStringExtra(EXTRA_TEXT)
    startActivity(EditorActivity.intent(this, preFilledNote))
    finish()
  }
}
