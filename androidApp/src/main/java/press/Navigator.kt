package press

import android.app.Activity
import android.content.ContextWrapper
import android.view.View
import me.saket.press.shared.ui.Navigator
import me.saket.press.shared.ui.ScreenKey

fun View.navigator(): Navigator {
  return object : Navigator {
    override fun lfg(screen: ScreenKey) {
      error("Can't navigate to $screen")
    }

    override fun goBack() {
      findActivity().finish()
    }
  }
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

fun View.findActivity(): Activity {
  var context = context
  while (context is ContextWrapper) {
    if (context is Activity) {
      return context
    }
    context = context.baseContext
  }
  error("Can't find activity, context: $context")
}
