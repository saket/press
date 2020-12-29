package press

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.getSystemService
import me.saket.press.shared.localization.strings

object Clipboards {
  fun copyPlainText(context: Context, text: String) {
    val manager = context.getSystemService<ClipboardManager>()!!
    manager.setPrimaryClip(ClipData.newPlainText(null, text))

    Toast.makeText(context, context.strings().editor.note_copied, Toast.LENGTH_SHORT).show()
  }
}
