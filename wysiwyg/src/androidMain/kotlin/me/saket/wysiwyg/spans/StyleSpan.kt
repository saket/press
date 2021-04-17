package me.saket.wysiwyg.spans

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

/**
 * Copy of [android.text.style.StyleSpan].
 */
class StyleSpan(val recycler: Recycler) : MetricAffectingSpan(), WysiwygSpan {

  enum class Style(val typefaceInt: Int) {
    NORMAL(Typeface.NORMAL),
    BOLD(Typeface.BOLD),
    ITALIC(Typeface.ITALIC)
  }

  @get:JvmName("style")
  var style: Style = Style.NORMAL

  override fun updateMeasureState(textPaint: TextPaint) {
    apply(textPaint, style)
  }

  override fun updateDrawState(textPaint: TextPaint) {
    apply(textPaint, style)
  }

  @SuppressLint("WrongConstant")
  private fun apply(
    paint: Paint,
    style: Style
  ) {
    val old = paint.typeface
    val oldStyle: Int = old?.style ?: 0

    val want = oldStyle or style.typefaceInt

    val tf: Typeface = when (old) {
      null -> Typeface.defaultFromStyle(want)
      else -> Typeface.create(old, want)
    }

    val fake = want and tf.style.inv()

    if (fake and Typeface.BOLD != 0) {
      paint.isFakeBoldText = true
    }

    if (fake and Typeface.ITALIC != 0) {
      paint.textSkewX = -0.25f
    }

    paint.typeface = tf
  }

  override fun recycle() {
    recycler(this)
  }
}
