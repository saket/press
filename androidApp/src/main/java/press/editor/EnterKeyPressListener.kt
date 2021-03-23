package press.editor

import android.text.InputFilter
import android.text.Selection
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.widget.EditText
import me.saket.wysiwyg.formatting.AutoFormatOnEnterPress
import me.saket.wysiwyg.formatting.TextSelection

/**
 * Detects presence of a new line character when the text is changed. This isn't ideal and it'd be great if Press
 * could simply rely on ACTION_ENTER ime events, but the keyboard world on Android is strange place where there are
 * no contracts and they can do whatever they feel like.
 */
class FormatMarkdownOnEnterPress(private val view: EditText) : InputFilter {
  var ignoreNextFilter = false

  override fun filter(
    source: CharSequence?,
    start: Int,
    end: Int,
    dest: Spanned,
    dstart: Int,
    dend: Int
  ): CharSequence? {
    if (ignoreNextFilter || source == null) {
      ignoreNextFilter = false
      return null
    }

    // Try to detect changes where only one new character was added, and it
    // was a new line. This is to ignore cases where some text was pasted.
    val sourceLength = end - start
    val destLength = dend - dstart
    val wasNewLineEntered = sourceLength - destLength == 1 && source.endsWith("\n")

    return if (wasNewLineEntered) {
      source.removeSuffix("\n").also {
        view.post {
          ignoreNextFilter = true
          replaceTextOnEnterPress(view, dest)
        }
      }
    } else {
      null
    }
  }

  fun replaceTextOnEnterPress(view: EditText, textBeforeEnter: Spanned) {
    val replacement = AutoFormatOnEnterPress.onEnter(
      textBeforeEnter = textBeforeEnter,
      cursorBeforeEnter = TextSelection.cursor(Selection.getSelectionStart(textBeforeEnter))
    ) ?: return

    view.text = SpannableStringBuilder(replacement.replacement).apply {
      view.text.copyWysiwygSpansTo(this)
    }
    replacement.newSelection?.let {
      view.setSelection(it.start, it.end)
    }
  }
}
