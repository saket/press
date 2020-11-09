package press.navigation

import press.home.HomeActivity

fun interface BackPressInterceptor {
  fun onInterceptBackPress(): InterceptResult

  enum class InterceptResult {
    Intercepted,
    Ignored
  }
}

interface ScreenFocusChangeListener {
  /** True when a View is present at the foreground in [HomeActivity]. False otherwise. */
  fun onScreenFocusChanged(hasFocus: Boolean)
}
