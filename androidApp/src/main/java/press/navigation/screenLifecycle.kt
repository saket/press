package press.navigation

import me.saket.press.shared.ui.ScreenKey

fun interface BackPressInterceptor {
  fun onInterceptBackPress(): InterceptResult

  enum class InterceptResult {
    Intercepted,
    Ignored
  }
}

interface ScreenFocusChangeListener {
  /** True when a View is present at the foreground of [TheActivity]. False otherwise. */
  fun onScreenFocusChanged(focusedScreen: ScreenKey?)
}

