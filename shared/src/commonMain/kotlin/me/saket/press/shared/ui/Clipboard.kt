package me.saket.press.shared.ui

interface Clipboard {
  fun copyPlainText(text: String)
  fun copyRichText(htmlText: String)
}
