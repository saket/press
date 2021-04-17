package me.saket.wysiwyg.widgets

import android.text.Editable
import android.widget.EditText

actual typealias NativeTextField = EditText

@Suppress("ConflictingExtensionProperty")
actual val NativeTextField.text: EditableText
  get() = text

fun EditText.addTextChangedListener(afterTextChange: AfterTextChange) {
  val watcher = object : SimpleTextWatcher {
    override fun afterTextChanged(text: Editable) {
      if (afterTextChange.isAvoidingInfiniteLoop.not()) {
        afterTextChange.callback(afterTextChange, text)
      }
    }
  }
  addTextChangedListener(watcher)

  // Initial event.
  watcher.afterTextChanged(text)
}
