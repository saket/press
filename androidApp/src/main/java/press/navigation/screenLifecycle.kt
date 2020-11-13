package press.navigation

import me.saket.press.shared.ui.ScreenKey
import press.home.HomeActivity

fun interface BackPressInterceptor {
  fun onInterceptBackPress(): InterceptResult

  enum class InterceptResult {
    Intercepted,
    Ignored
  }
}

interface ScreenFocusChangeListener {
  /** True when a View is present at the foreground of [HomeActivity]. False otherwise. */
  fun onScreenFocusChanged(hasFocus: Boolean, focusedScreen: ScreenKey?)
}
