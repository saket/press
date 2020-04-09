package press

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import press.editor.EditorActivity

class ShortcutReceiverActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    startActivity(EditorActivity.intent(this))
    finish()
  }
}
