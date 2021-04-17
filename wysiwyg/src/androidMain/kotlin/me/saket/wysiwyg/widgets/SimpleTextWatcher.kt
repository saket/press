package me.saket.wysiwyg.widgets

import android.text.Editable
import android.text.TextWatcher

interface SimpleTextWatcher : TextWatcher {
  override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) = Unit
  override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) = Unit
  override fun afterTextChanged(text: Editable) = Unit
}
