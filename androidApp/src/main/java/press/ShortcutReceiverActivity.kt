package press

import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.saket.press.shared.db.NoteId
import me.saket.press.shared.editor.EditorOpenMode.NewNote
import me.saket.press.shared.editor.EditorScreenKey
import press.navigation.TheActivity

class ShortcutReceiverActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val editorScreen = EditorScreenKey(
      openMode = NewNote(
        placeholderId = NoteId.generate(),
        preFilledNote = intent.getStringExtra(EXTRA_TEXT)
      ),
      showKeyboard = true
    )
    startActivity(TheActivity.screenIntent(this, editorScreen))
    finish()
  }
}
