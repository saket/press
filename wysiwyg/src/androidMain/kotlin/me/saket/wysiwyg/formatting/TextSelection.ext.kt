package me.saket.wysiwyg.formatting

import android.widget.EditText

fun TextSelection.Companion.from(view: EditText): TextSelection? {
  return if (view.selectionStart >= 0 && view.selectionEnd >= 0) {
    TextSelection(view.selectionStart, view.selectionEnd)
  } else {
    null
  }
}
