package press.widgets

import android.text.SpanWatcher
import android.text.Spannable

class OnSpanAdded(val listener: (Spannable, Any) -> Unit) : SpanWatcher {

  override fun onSpanChanged(
    text: Spannable?,
    what: Any?,
    ostart: Int,
    oend: Int,
    nstart: Int,
    nend: Int
  ) = Unit

  override fun onSpanRemoved(text: Spannable, what: Any?, start: Int, end: Int) = Unit

  override fun onSpanAdded(text: Spannable, what: Any, start: Int, end: Int) {
    listener(text, what)
  }
}
