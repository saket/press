package me.saket.press.shared.ui

interface IntentLauncher {
  fun sharePlainText(text: String)
  fun shareRichText(htmlText: String)
}
