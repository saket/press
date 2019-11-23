package me.saket.wysiwyg.spans

import android.graphics.Paint.FontMetricsInt
import android.text.TextPaint
import android.text.style.LineHeightSpan
import android.text.style.UpdateLayout

/**
 * PSA: extending [UpdateLayout] is necessary or else StaticLayout won't render
 * line height spans if they're inserted asynchronously until the line is edited again.
 */
interface AndroidLineHeightSpan : LineHeightSpan.WithDensity, UpdateLayout {

  fun chooseHeight(lineHeight: Int, fm: FontMetricsInt, paint: TextPaint)

  override fun chooseHeight(
    text: CharSequence,
    start: Int,
    end: Int,
    spanstartv: Int,
    lineHeight: Int,
    fm: FontMetricsInt,
    paint: TextPaint
  ) {
    chooseHeight(lineHeight, fm, paint)
  }

  override fun chooseHeight(
    text: CharSequence,
    start: Int,
    end: Int,
    spanstartv: Int,
    lineHeight: Int,
    fm: FontMetricsInt
  ) {
    throw error("Should never get called as long as the other chooseHeight() is overridden.")
  }
}
