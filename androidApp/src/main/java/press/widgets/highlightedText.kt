package press.widgets

import android.text.SpannableString
import me.saket.press.shared.ui.HighlightedText

fun HighlightedText.withSpan(span: Any): CharSequence {
  return when (val it = highlight) {
    null -> return text
    else -> SpannableString(text).apply {
      setSpan(span, it.first, it.last, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
  }
}
