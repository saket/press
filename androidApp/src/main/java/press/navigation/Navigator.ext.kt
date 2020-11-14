package press.navigation

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.ScreenKey

interface HasNavigator {
  val navigator: Navigator
}

fun View.navigator(): Navigator {
  var context = context
  while (context is ContextWrapper) {
    if (context is HasNavigator) {
      return context.navigator
    }
    context = context.baseContext
  }
  error("Can't find navigator in context: $context")
}

inline fun <reified T : ScreenKey> Navigator.handle(crossinline handler: (T) -> Unit): Navigator {
  val delegate = this
  return object : Navigator by delegate {
    override fun lfg(screen: ScreenKey) {
      when (screen) {
        is T -> handler(screen)
        else -> delegate.lfg(screen)
      }
    }
  }
}
