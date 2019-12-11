package press.home

import android.os.Bundle
import me.saket.press.R
import press.App
import press.widgets.BackPressInterceptResult.BACK_PRESS_IGNORED
import press.widgets.ThemeAwareActivity
import javax.inject.Inject

class HomeActivity : ThemeAwareActivity() {

  @field:Inject
  lateinit var homeViewFactory: HomeView.Factory
  lateinit var homeView: HomeView

  override fun onCreate(savedInstanceState: Bundle?) {
    App.component.inject(this)
    super.onCreate(savedInstanceState)
    homeView = homeViewFactory.withContext(this)
    homeView.id = R.id.homeViewId
    setContentView(homeView)
  }

  override fun onBackPressed() {
    if (homeView.offerBackPress() == BACK_PRESS_IGNORED) {
      super.onBackPressed()
    }
  }
}
