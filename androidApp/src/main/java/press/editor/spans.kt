package press.editor

import android.text.Editable
import android.text.Spannable
import androidx.core.text.getSpans

fun Editable.copySpansTo(other: Editable) {
  val allSpans = this.getSpans<Any>(0, this.length)
  for (span in allSpans) {
    val spanStart = this.getSpanStart(span)
    val spanEnd = this.getSpanEnd(span)
    other.setSpan(span, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }
}
