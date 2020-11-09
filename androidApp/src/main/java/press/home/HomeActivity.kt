package press.home

import android.content.Context
import android.os.Bundle
import android.widget.FrameLayout
import me.saket.press.shared.home.HomeScreenKey
import press.PressApp
import press.extensions.unsafeLazy
import press.navigation.BackPressInterceptor.InterceptResult.Ignored
import press.navigation.HasNavigator
import press.navigation.RealNavigator
import press.navigation.ScreenKeyChanger
import press.widgets.ThemeAwareActivity

class HomeActivity : ThemeAwareActivity(), HasNavigator {
  private lateinit var screenChanger: ScreenKeyChanger
  override lateinit var navigator: RealNavigator
  private val navHostView by unsafeLazy { FrameLayout(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(navHostView)
  }

  override fun attachBaseContext(newBase: Context) {
    screenChanger = ScreenKeyChanger(
      container = { navHostView },
      viewFactories = PressApp.component.viewFactories()
    )
    navigator = RealNavigator(this, screenChanger)
    super.attachBaseContext(navigator.installInContext(newBase, HomeScreenKey))
  }

  override fun onBackPressed() {
    if (screenChanger.onInterceptBackPress() == Ignored) {
      if (!navigator.goBack()) {
        super.onBackPressed()
      }
    }
  }
}
