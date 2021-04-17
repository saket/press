package me.saket.wysiwyg.spans

import android.graphics.Paint.FontMetricsInt
import android.text.TextPaint
import android.text.style.LineHeightSpan

interface SimpleLineHeightSpan : LineHeightSpan.WithDensity {

  fun chooseHeight(
    text: CharSequence,
    lineStart: Int,
    lineEnd: Int,
    lineHeight: Int,
    fm: FontMetricsInt,
    paint: TextPaint
  )

  override fun chooseHeight(
    text: CharSequence,
    start: Int,
    end: Int,
    spanstartv: Int,
    lineHeight: Int,
    fm: FontMetricsInt,
    paint: TextPaint
  ) {
    chooseHeight(text, start, end, lineHeight, fm, paint)
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
