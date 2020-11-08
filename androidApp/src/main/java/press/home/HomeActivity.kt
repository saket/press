package press.home

import android.content.Context
import android.view.Window
import press.PressApp
import press.navigation.BackPressInterceptor.InterceptResult.Ignored
import press.navigation.RealNavigator2
import press.navigation.ScreenKeyChanger
import press.navigation.ViewFactories
import press.widgets.ThemeAwareActivity
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class HomeActivity : ThemeAwareActivity() {
  @Inject lateinit var viewFactories: ViewFactories

  private val screenChanger by lazy(NONE) {
    ScreenKeyChanger(
      container = { findViewById(Window.ID_ANDROID_CONTENT) },
      viewFactories = viewFactories
    )
  }
  private val navigator2 by lazy(NONE) {
    RealNavigator2(this, screenChanger)
  }

  override fun attachBaseContext(newBase: Context) {
    PressApp.component.inject(this)
    super.attachBaseContext(navigator2.installInContext(newBase, HomeScreenKey()))
  }

  override fun onBackPressed() {
    if (screenChanger.onInterceptBackPress() == Ignored) {
      super.onBackPressed()
    }
  }
}
