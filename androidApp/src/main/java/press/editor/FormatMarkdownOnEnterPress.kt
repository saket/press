package press.editor

import android.text.InputFilter
import android.text.Selection
import android.text.Spanned
import me.saket.wysiwyg.formatting.AutoFormatOnEnterPress
import me.saket.wysiwyg.formatting.TextSelection

/**
 * Detects presence of a new line character when the text is changed. This isn't ideal and it'd be great if Press
 * could simply rely on ACTION_ENTER ime events, but the keyboard world on Android is a strange place where there
 * are no contracts and keyboards can do whatever they feel like.
 */
class FormatMarkdownOnEnterPress(private val view: MarkdownEditText) : InputFilter {
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

    if (wasNewLineEntered) {
      val replacement = AutoFormatOnEnterPress.onEnter(
        textBeforeEnter = dest,
        cursorBeforeEnter = TextSelection.cursor(Selection.getSelectionStart(dest))
      )
      if (replacement != null) {
        // Can't change the text here so will have to post this to the end of the View's queue.
        view.post {
          ignoreNextFilter = true
          view.setTextWithoutBustingUndoHistory(replacement.replacement, replacement.newSelection)
        }
      }
    }

    // Return null to accept this text change.
    return null
  }
}
