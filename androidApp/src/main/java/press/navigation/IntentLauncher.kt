package press.navigation

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_SYSTEM
import androidx.core.app.ShareCompat
import me.saket.press.shared.localization.strings
import me.saket.press.shared.ui.IntentLauncher

class RealIntentLauncher(val context: Activity) : IntentLauncher {
  override fun sharePlainText(text: String) {
    ShareCompat.IntentBuilder.from(context)
      .setType("text/plain")
      .setText(text)
      .setChooserTitle(context.strings().common.share_picker_title)
      .startChooser()
  }

  override fun shareRichText(htmlText: String) {
    ShareCompat.IntentBuilder.from(context)
      .setType("text/html")
      .setHtmlText(htmlText)
      .setChooserTitle(context.strings().common.share_picker_title)
      .startChooser()
  }

  override fun openUrl(url: String) {
    CustomTabsIntent.Builder()
      .setShowTitle(true)
      .addDefaultShareMenuItem()
      .enableUrlBarHiding()
      .setColorScheme(COLOR_SCHEME_SYSTEM)
      .build()
      .launchUrl(context, Uri.parse(url))
  }
}
