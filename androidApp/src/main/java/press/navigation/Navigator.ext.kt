package press.navigation

import android.content.ContextWrapper
import android.view.View
import me.saket.press.shared.ui.Navigator

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
