package press.home

import android.os.Bundle
import press.App
import press.widgets.BackPressInterceptResult.BACK_PRESS_IGNORED
import press.widgets.ThemeAwareActivity
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class HomeActivity : ThemeAwareActivity() {

  @Inject lateinit var homeViewFactory: HomeView.Factory
  private val homeView by lazy(NONE) { homeViewFactory.withContext(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    App.component.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(homeView)
  }

  override fun onBackPressed() {
    if (homeView.offerBackPress() == BACK_PRESS_IGNORED) {
      super.onBackPressed()
    }
  }
}
