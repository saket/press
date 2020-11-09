package press.home

import android.content.Context
import android.view.Window
import me.saket.press.shared.home.HomeScreenKey
import press.PressApp
import press.extensions.unsafeLazy
import press.navigation.BackPressInterceptor.InterceptResult.Ignored
import press.navigation.HasNavigator
import press.navigation.RealNavigator
import press.navigation.ScreenKeyChanger
import press.navigation.ViewFactories
import press.widgets.ThemeAwareActivity
import javax.inject.Inject

class HomeActivity : ThemeAwareActivity(), HasNavigator {
  @Inject lateinit var viewFactories: ViewFactories

  private val screenChanger by unsafeLazy {
    ScreenKeyChanger(
      container = { findViewById(Window.ID_ANDROID_CONTENT) },
      viewFactories = viewFactories
    )
  }
  override val navigator by unsafeLazy {
    RealNavigator(this, screenChanger)
  }

  override fun attachBaseContext(newBase: Context) {
    PressApp.component.inject(this)
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
