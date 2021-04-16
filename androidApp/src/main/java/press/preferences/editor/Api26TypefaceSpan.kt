package press.preferences.editor

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.text.style.TypefaceSpan

/** Because [TypefaceSpan] doesn't take a [Typeface] on < API 28. */
class Api26TypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
  override fun updateDrawState(ds: TextPaint) {
    updateTypeface(ds)
  }

  override fun updateMeasureState(paint: TextPaint) {
    updateTypeface(paint)
  }

  private fun updateTypeface(paint: Paint) {
    paint.typeface = typeface
  }
}
