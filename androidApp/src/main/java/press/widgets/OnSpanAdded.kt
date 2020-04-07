package press.widgets

import android.text.SpanWatcher
import android.text.Spannable

interface SimpleSpanWatcher : SpanWatcher {
  override fun onSpanChanged(text: Spannable?, what: Any?, ostart: Int, oend: Int, nstart: Int, nend: Int) = Unit
  override fun onSpanRemoved(text: Spannable, what: Any?, start: Int, end: Int) = Unit
  override fun onSpanAdded(text: Spannable, what: Any, start: Int, end: Int) = onSpanAdded(text, what)

  fun onSpanAdded(text: Spannable, span: Any)
}
