package press.navigation

fun interface BackPressInterceptor {
  fun onInterceptBackPress(): InterceptResult

  enum class InterceptResult {
    Intercepted,
    Ignored
  }
}
