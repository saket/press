package compose.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import compose.ComposeApp
import compose.widgets.ThemeAwareActivity
import javax.inject.Inject

class EditorActivity : ThemeAwareActivity() {

  @field:Inject
  lateinit var editorView: EditorView.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    ComposeApp.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(editorView.withContext(this))
  }

  companion object {
    fun intent(context: Context): Intent = Intent(context, EditorActivity::class.java)
  }
}