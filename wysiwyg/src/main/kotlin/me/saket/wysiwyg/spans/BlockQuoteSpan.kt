package me.saket.wysiwyg.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.style.LeadingMarginSpan
import me.saket.wysiwyg.WysiwygTheme
import kotlin.math.max
import kotlin.math.min

/**
 * Copied from https://github.com/noties/Markwon.
 */
class BlockQuoteSpan(
  val theme: WysiwygTheme,
  val recycler: Recycler
) : LeadingMarginSpan, WysiwygSpan {

  private val marginRect = COMMON_RECT
  private val marginPaint = COMMON_PAINT

  override fun getLeadingMargin(first: Boolean) = theme.blockQuoteIndentationMargin

  override fun drawLeadingMargin(
    c: Canvas,
    p: Paint,
    x: Int,
    dir: Int,
    top: Int,
    baseline: Int,
    bottom: Int,
    text: CharSequence?,
    start: Int,
    end: Int,
    first: Boolean,
    layout: Layout?
  ) {
    marginPaint.set(p)
    marginPaint.style = Paint.Style.FILL
    marginPaint.color = theme.blockQuoteVerticalRuleColor

    val width = theme.blockQuoteVerticalRuleStrokeWidth
    val l = x + dir * width
    val r = l + dir * width
    val left = min(l, r)
    val right = max(l, r)
    marginRect.set(left, top, right, bottom)
    c.drawRect(marginRect, marginPaint)
  }

  override fun recycle() {
    recycler(this)
  }

  companion object {
    val COMMON_RECT = Rect()
    val COMMON_PAINT = Paint()
  }
}