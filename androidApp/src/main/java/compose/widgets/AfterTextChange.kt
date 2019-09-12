package compose.widgets

import android.text.Editable
import android.text.TextWatcher

class AfterTextChange(val listener: (Editable) -> Unit) : TextWatcher {
  override fun afterTextChanged(s: Editable) = listener(s)
  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
}
