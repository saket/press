package me.saket.press.shared.ui

class FakeIntentLauncher : IntentLauncher {
  override fun sharePlainText(text: String) = Unit
  override fun shareRichText(htmlText: String) = Unit
  override fun openUrl(url: String) = Unit
}
