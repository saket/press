package compose.home

import android.os.Bundle
import compose.ComposeApp
import compose.widgets.ThemeAwareActivity
import javax.inject.Inject

class HomeActivity : ThemeAwareActivity() {

  @field:Inject
  lateinit var homeView: HomeView.Factory

  override fun onCreate(savedInstanceState: Bundle?) {
    ComposeApp.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(homeView.withContext(this))
  }
}
