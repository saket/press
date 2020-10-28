package press.editor

import android.text.InputFilter
import android.text.Selection
import android.text.Spanned
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_DEL
import android.widget.EditText
import me.saket.wysiwyg.formatting.AutoFormatOnEnterPress
import me.saket.wysiwyg.formatting.ReplaceNewLineWith.DeleteLetters
import me.saket.wysiwyg.formatting.ReplaceNewLineWith.InsertLetters
import me.saket.wysiwyg.formatting.TextSelection

class FormatMarkdownOnEnterPress(private val view: EditText) : EnterKeyDetector() {

  override fun onEnterPress(textBeforeEnter: Spanned): CharSequence? {
    val replacement = AutoFormatOnEnterPress.onEnter(
      textBeforeEnter = textBeforeEnter,
      cursorBeforeEnter = TextSelection.cursor(Selection.getSelectionStart(textBeforeEnter))
    ) ?: return null

    return when (replacement) {
      is InsertLetters -> {
        view.post { view.setSelection(replacement.newSelection.start, replacement.newSelection.end) }
        replacement.replacement
      }
      is DeleteLetters -> {
        repeat(replacement.deleteCount) {
          view.dispatchKeyEvent(KeyEvent(ACTION_DOWN, KEYCODE_DEL))
          view.dispatchKeyEvent(KeyEvent(ACTION_UP, KEYCODE_DEL))
        }
        ""
      }
    }
  }
}

abstract class EnterKeyDetector : InputFilter {
  override fun filter(
    source: CharSequence,
    start: Int,
    end: Int,
    dest: Spanned,
    dstart: Int,
    dend: Int
  ): CharSequence? {
    val enterPressed = source == "\n" && end - start == 1
    return when {
      enterPressed -> onEnterPress(textBeforeEnter = dest)
      else -> null  // Accept the original change.
    }
  }

  abstract fun onEnterPress(textBeforeEnter: Spanned): CharSequence?
}
