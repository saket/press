package press.editor

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build.VERSION.SDK_INT
import android.text.Spanned
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText

/**
 * Removes rich text formatting for pasted content. On Android, copying a
 * URL will implicitly also copy its underline span, which we don't want.
 */
@SuppressLint("AppCompatCustomView")  // Cursor tinting doesn't work for some reason.
class PlainTextPasteEditText(context: Context) : EditText(context) {

  override fun onTextContextMenuItem(id: Int): Boolean {
    if (id == android.R.id.paste) {
      if (SDK_INT >= 23) {
        return super.onTextContextMenuItem(android.R.id.pasteAsPlainText)
      } else {
        replaceClipboardWithPlainText()
      }
    }
    return super.onTextContextMenuItem(id)
  }

  private fun replaceClipboardWithPlainText() {
    val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val primaryClip = clipboard.primaryClip

    // Not sure if Android let's the user copy multiple items. Assume it to always be 1.
    val primaryClipItem = primaryClip?.getItemAt(0)
    require(primaryClipItem != null) { "Why would we get a paste event with null clip data?" }

    val text = primaryClipItem.coerceToText(context)
    val plainText = if (text is Spanned) text.toString() else text

    val clipData = ClipData.newPlainText(primaryClip.description.label, plainText)
    clipboard.setPrimaryClip(clipData)
  }
}
