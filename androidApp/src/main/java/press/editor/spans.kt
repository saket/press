package press.editor

import android.text.Editable
import android.text.Spannable
import androidx.core.text.getSpans
import me.saket.wysiwyg.spans.WysiwygSpan

fun Editable.copyWysiwygSpansTo(other: Editable) {
  val allSpans = this.getSpans<WysiwygSpan>(0, this.length)
  for (span in allSpans) {
    val spanStart = this.getSpanStart(span)
    val spanEnd = this.getSpanEnd(span)
    if (spanEnd < other.length) {
      other.setSpan(span, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
  }
}
