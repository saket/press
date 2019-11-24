package me.saket.wysiwyg.widgets

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

actual typealias NativeTextField = EditText

@Suppress("ConflictingExtensionProperty")
actual val NativeTextField.text: EditableText
  get() = text

fun EditText.addTextChangedListener(afterTextChange: AfterTextChange) {
  val watcher = object : TextWatcher {
    override fun afterTextChanged(s: Editable) {
      if (afterTextChange.isAvoidingInfiniteLoop.not()) {
        afterTextChange.callback(afterTextChange, s)
      }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
  }
  addTextChangedListener(watcher)

  // Initial event.
  watcher.afterTextChanged(text)
}
