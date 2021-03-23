package press.editor

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.text.Editable
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
import android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY
import android.text.SpannableStringBuilder
import android.view.Gravity.TOP
import android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN
import androidx.appcompat.widget.AppCompatEditText

/**
 * Removes rich text formatting for pasted content. On Android, copying a
 * URL will implicitly also copy its underline span, which we don't want.
 */
class MarkdownEditText(context: Context) : AppCompatEditText(context) {
  init {
    background = null
    breakStrategy = BREAK_STRATEGY_HIGH_QUALITY
    gravity = TOP
    inputType = TYPE_CLASS_TEXT or  // Multiline doesn't work without this.
      TYPE_TEXT_FLAG_CAP_SENTENCES or
      TYPE_TEXT_FLAG_MULTI_LINE or
      TYPE_TEXT_FLAG_NO_SUGGESTIONS
    imeOptions = IME_FLAG_NO_FULLSCREEN
    if (SDK_INT >= 26) {
      importantForAutofill = IMPORTANT_FOR_AUTOFILL_NO
    }

    filters += FormatMarkdownOnEnterPress(this)
    addTextChangedListener(CapitalizeOnHeadingStart(this))

    setEditableFactory(object : Editable.Factory() {
      override fun newEditable(source: CharSequence): Editable {
        return when (source) {
          is Editable -> source // Avoid creating a new object on every external text change.
          else -> SpannableStringBuilder(source)
        }
      }
    })
  }

  override fun onTextContextMenuItem(id: Int): Boolean {
    // Remove rich text formatting for pasted content. For example, pasting a URL from
    // another app may implicitly also copy its underline span, which we don't want.
    return if (id == android.R.id.paste) {
      super.onTextContextMenuItem(android.R.id.pasteAsPlainText)
    } else {
      super.onTextContextMenuItem(id)
    }
  }
}
