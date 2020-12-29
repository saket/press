package me.saket.press.shared.ui

class FakeClipboard : Clipboard {
  override fun copyPlainText(text: String) = Unit
  override fun copyRichText(htmlText: String) = Unit
}
