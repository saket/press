package compose.home

import android.os.Bundle
import compose.ComposeApp
import compose.widgets.BackPressInterceptResult.BACK_PRESS_IGNORED
import compose.widgets.ThemeAwareActivity
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class HomeActivity : ThemeAwareActivity() {

  @field:Inject
  lateinit var homeViewFactory: HomeView.Factory
  private val homeView by lazy(NONE) { homeViewFactory.withContext(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    ComposeApp.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(homeView)
  }

  override fun onBackPressed() {
    if (homeView.offerBackPress() == BACK_PRESS_IGNORED) {
      super.onBackPressed()
    }
  }
}
