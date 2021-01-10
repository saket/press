package me.saket.press.shared.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Html

class RealClipboard(private val appContext: Context) : Clipboard {
  override fun copyPlainText(text: String) {
    val manager = appContext.getSystemService(ClipboardManager::class.java)!!
    manager.setPrimaryClip(ClipData.newPlainText(null, text))
  }

  @Suppress("DEPRECATION")
  override fun copyRichText(htmlText: String) {
    val manager = appContext.getSystemService(ClipboardManager::class.java)!!
    manager.setPrimaryClip(ClipData.newPlainText(null, Html.fromHtml(htmlText)))
  }
}
