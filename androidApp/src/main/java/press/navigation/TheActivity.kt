package press.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.ScreenKey
import press.PressApp
import press.extensions.unsafeLazy
import press.widgets.ThemeAwareActivity

class TheActivity : ThemeAwareActivity(), HasNavigator {
  override lateinit var navigator: Navigator
  private val navHostView by unsafeLazy { FrameLayout(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(navHostView)
  }

  override fun attachBaseContext(newBase: Context) {
    val screenChanger = ScreenKeyChanger(
      hostView = { navHostView },
      viewFactories = PressApp.component.viewFactories(),
      transitions = listOf(
        ExpandableScreenTransition(),
        MorphFromFabScreenTransition()
      )
    )
    navigator = RealNavigator(this, screenChanger).also {
      super.attachBaseContext(it.installInContext(newBase, PlaceholderScreenKey()))
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    (readInitialScreen(intent) ?: HomeScreenKey()).let {
      navigator.clearTopAndLfg(it)
    }
  }

  override fun onBackPressed() {
    if (!navigator.goBack()) {
      super.onBackPressed()
    }
  }

  companion object {
    private const val EXTRA_SCREEN_KEY = "press:screenKey"

    fun readInitialScreen(intent: Intent): ScreenKey? {
      return intent.getParcelableExtra(EXTRA_SCREEN_KEY)
    }

    fun intent(context: Context, initialScreen: ScreenKey): Intent {
      return Intent(context, TheActivity::class.java).putExtra(EXTRA_SCREEN_KEY, initialScreen)
    }
  }
}
