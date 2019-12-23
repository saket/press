package me.saket.wysiwyg.formatting

import android.widget.EditText

fun TextSelection.Companion.from(view: EditText): TextSelection {
  return TextSelection(view.selectionStart, view.selectionEnd)
}

