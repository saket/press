package me.saket.wysiwyg.spans

import android.graphics.Paint.FontMetricsInt
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import me.saket.wysiwyg.parser.node.HeadingLevel

/**
 * Copied from https://github.com/noties/Markwon.
 */
open class HeadingSpan(
  val recycler: Recycler
) : MetricAffectingSpan(), AndroidLineHeightSpan, WysiwygSpan {

  lateinit var level: HeadingLevel

  override fun updateMeasureState(textPaint: TextPaint) = apply(textPaint)

  override fun updateDrawState(textPaint: TextPaint) = apply(textPaint)

  private fun apply(paint: TextPaint) {
    paint.isFakeBoldText = true
    paint.textSize *= level.textSizeRatio
  }

  override fun chooseHeight(lineHeight: Int, fm: FontMetricsInt, paint: TextPaint) {
    val spacing = (level.textSizeRatio * paint.density).toInt()
    fm.descent *= spacing
    fm.bottom *= spacing
  }

  override fun recycle() {
    recycler(this)
  }
}
