package press.editor

import android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
import android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
import android.text.Spannable
import android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE
import android.widget.EditText
import androidx.core.text.getSpans
import me.saket.press.shared.editor.EditorPresenter.Companion.NEW_NOTE_PLACEHOLDER
import me.saket.wysiwyg.spans.HeadingSpan
import me.saket.wysiwyg.widgets.SimpleTextWatcher
import press.widgets.SimpleSpanWatcher
import java.lang.Character.isWhitespace

/**
 * Uses [TYPE_TEXT_FLAG_CAP_WORDS] for the first letter after '# ' so that the
 * heading starts with a capital letter and [TYPE_TEXT_FLAG_CAP_SENTENCES] for
 * the rest of the content.
 */
class CapitalizeOnHeadingStart(val editText: EditText) : SimpleSpanWatcher, SimpleTextWatcher {
  private val originalInputType = editText.inputType
  private var multipleCharactersRemoved: Boolean = false
  private var safeToChangeInputType: Boolean = false

  companion object {
    private const val headingSyntax = '#'
  }

  override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
    // Multiple letters are an indication that the text was
    // selected. Changing the input type here will cause any
    // ongoing text prediction to be reset.
    multipleCharactersRemoved = count > 1
  }

  override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
    // Avoid changing input type if,
    // - a character was deleted (count == 0)
    // - a digit/symbol was inserted.
    safeToChangeInputType = text.toString() == NEW_NOTE_PLACEHOLDER
      || multipleCharactersRemoved.not()
      && (count == 0 || text[start].isLetter() || text[start].isWhitespace())

    // Wysiwyg inserts spans without changing the text. Add a
    // SpanWatcher and wait for the Markdown to be processed.
    (text as Spannable).setSpan(this, 0, text.length, SPAN_INCLUSIVE_INCLUSIVE)
  }

  override fun onSpanAdded(text: Spannable, span: Any) {
    if (span is HeadingSpan) {
      onHeadingSpanAdded(text)
    }
  }

  fun onHeadingSpanAdded(text: Spannable) {
    if (safeToChangeInputType.not()) {
      return
    }

    val forceCapitalize = if (safeToChangeInputType && text.length >= headingSyntax.length) {
      val cursorAt = editText.selectionEnd
      val headingsUnderSpan = text.getSpans<HeadingSpan>(
        start = cursorAt - headingSyntax.length,
        end = cursorAt
      )
      val atStartOfSyntax = cursorAt >= 2 && text[cursorAt - 2] == headingSyntax && isWhitespace(text[cursorAt - 1])
      headingsUnderSpan.isNotEmpty() && atStartOfSyntax

    } else {
      false
    }

    val newInputType = when {
      forceCapitalize ->
        originalInputType
          .xor(TYPE_TEXT_FLAG_CAP_SENTENCES)
          .or(TYPE_TEXT_FLAG_CAP_WORDS)
      else -> originalInputType
    }

    if (newInputType != editText.inputType) {
      editText.inputType = newInputType
    }
  }

  @Suppress("unused")
  private val Char.length: Int
    get() = 1
}
