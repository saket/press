package home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import theme.DarculaTheme

class HomeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    theme().apply(this)
    super.onCreate(savedInstanceState)
    setContentView(EditorView(this, theme()))
  }

  private fun theme() = DarculaTheme
}