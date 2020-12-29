package me.saket.press.shared.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Html
import android.widget.Toast
import me.saket.press.shared.localization.strings

class RealClipboard(val context: Context) : Clipboard {
  override fun copyPlainText(text: String) {
    val manager = context.getSystemService(ClipboardManager::class.java)!!
    manager.setPrimaryClip(ClipData.newPlainText(null, text))

    Toast.makeText(context, context.strings().editor.note_copied, Toast.LENGTH_SHORT).show()
  }

  @Suppress("DEPRECATION")
  override fun copyRichText(htmlText: String) {
    val manager = context.getSystemService(ClipboardManager::class.java)!!
    manager.setPrimaryClip(ClipData.newPlainText(null, Html.fromHtml(htmlText)))

    Toast.makeText(context, context.strings().editor.note_copied, Toast.LENGTH_SHORT).show()
  }
}
