package press

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.app.ShareCompat
import me.saket.press.shared.localization.strings

object Intents {
  fun sharePlainText(context: Context, text: CharSequence) {
    ShareCompat.IntentBuilder.from(context.findActivity())
      .setType("text/plain")
      .setText(text.toString())
      .setChooserTitle(context.strings().common.share_picker_title)
      .startChooser()
  }
}

fun Context.findActivity(): Activity {
  var context = this
  while (context is ContextWrapper) {
    if (context is Activity) {
      return context
    }
    context = context.baseContext
  }
  error("Can't find Activity for context: $context")
}
