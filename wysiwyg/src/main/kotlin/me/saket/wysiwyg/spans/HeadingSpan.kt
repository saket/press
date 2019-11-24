package me.saket.wysiwyg.spans

import android.graphics.Paint.FontMetricsInt
import android.text.Spanned
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import me.saket.wysiwyg.parser.node.HeadingLevel

/**
 * Copied from https://github.com/noties/Markwon.
 */
open class HeadingSpan(
  val recycler: Recycler
) : MetricAffectingSpan(), SimpleLineHeightSpan, WysiwygSpan {

  lateinit var level: HeadingLevel

  override fun updateMeasureState(textPaint: TextPaint) = apply(textPaint)

  override fun updateDrawState(textPaint: TextPaint) = apply(textPaint)

  private fun apply(paint: TextPaint) {
    paint.isFakeBoldText = true
    paint.textSize *= level.textSizeRatio
  }

  /**
   * For multiple lines, chooseHeight() gets called for each line.
   */
  override fun chooseHeight(
    text: CharSequence,
    lineStart: Int,
    lineEnd: Int,
    lineHeight: Int,
    fm: FontMetricsInt,
    paint: TextPaint
  ) {
    val spanEnd = (text as Spanned).getSpanEnd(this)
    val isLastLine = spanEnd in lineStart..lineEnd

    if (isLastLine) {
      val spacing = (level.textSizeRatio * paint.density).toInt()
      fm.descent *= spacing
      fm.bottom *= spacing
    }
  }

  override fun recycle() {
    recycler(this)
  }
}
