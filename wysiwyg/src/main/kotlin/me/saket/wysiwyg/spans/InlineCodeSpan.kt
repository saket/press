package me.saket.wysiwyg.spans

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import me.saket.wysiwyg.WysiwygTheme

/**
 * Copied from https://github.com/noties/Markwon.
 */
class InlineCodeSpan(
  theme: WysiwygTheme,
  val recycler: Recycler
) : MetricAffectingSpan(), WysiwygSpan {

  private val codeBackgroundColor = theme.codeBackgroundColor

  override fun updateMeasureState(textPaint: TextPaint) {
    apply(textPaint)
  }

  override fun updateDrawState(textPaint: TextPaint) {
    apply(textPaint)
    textPaint.bgColor = codeBackgroundColor
  }

  private fun apply(paint: TextPaint) {
    paint.typeface = Typeface.MONOSPACE
    paint.textSize = paint.textSize * CODE_DEFINITION_TEXT_SIZE_RATIO
  }

  override fun recycle() {
    recycler(this)
  }

  companion object {
    internal const val CODE_DEFINITION_TEXT_SIZE_RATIO = .87f
  }
}
