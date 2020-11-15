package press.navigation

import android.view.View

fun interface BackPressInterceptor {
  fun onInterceptBackPress(): InterceptResult

  enum class InterceptResult {
    Intercepted,
    Ignored
  }
}

interface ScreenFocusChangeListener {
  /** True when a View is present at the foreground of [TheActivity]. False otherwise. */
  fun onScreenFocusChanged(focusedScreen: View?)
}
