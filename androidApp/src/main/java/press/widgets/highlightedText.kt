package press.widgets

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import me.saket.press.shared.ui.HighlightedText
import press.theme.themePalette

fun HighlightedText.withSpan(span: Any = ForegroundColorSpan(themePalette().accentColor)): CharSequence {
  return when (val it = highlight) {
    null -> return text
    else -> SpannableString(text).apply {
      setSpan(span, it.first, it.last, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
  }
}
