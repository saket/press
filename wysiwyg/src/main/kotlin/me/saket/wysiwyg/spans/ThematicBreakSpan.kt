package me.saket.wysiwyg.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan
import me.saket.wysiwyg.WysiwygTheme
import me.saket.wysiwyg.parser.highlighters.ThematicBreakSyntaxType
import me.saket.wysiwyg.parser.highlighters.ThematicBreakSyntaxType.ASTERISKS
import me.saket.wysiwyg.parser.highlighters.ThematicBreakSyntaxType.HYPHENS
import me.saket.wysiwyg.parser.highlighters.ThematicBreakSyntaxType.UNDERSCORES

actual class ThematicBreakSpan actual constructor(
  private val theme: WysiwygTheme,
  private val recycler: Recycler,
  actual val syntax: CharSequence,
  actual val syntaxType: ThematicBreakSyntaxType
) : LineBackgroundSpan, WysiwygSpan {

  private var offsetForSyntax = -1f

  override fun drawBackground(
    canvas: Canvas,
    paint: Paint,
    left: Int,
    right: Int,
    top: Int,
    baseline: Int,
    bottom: Int,
    ignored: CharSequence,
    start: Int,
    end: Int,
    lineNumber: Int
  ) {
    val originalPaintColor = paint.color
    paint.color = theme.thematicBreakColor
    paint.strokeWidth = theme.thematicBreakThickness

    if (offsetForSyntax == -1f) {
      offsetForSyntax = paint.measureText(syntax.toString())
    }

    val lineCenter = ((top + bottom) / 2 + paint.textSize * syntaxType.topOffsetFactor()).toInt()
    canvas.drawLine(
        left + offsetForSyntax,
        lineCenter.toFloat(),
        right.toFloat(),
        lineCenter.toFloat(),
        paint
    )

    paint.color = originalPaintColor
  }

  override fun recycle() {
    recycler(this)
  }

  /** Used for centering the rule with the text. */
  private fun ThematicBreakSyntaxType.topOffsetFactor(): Float =
    when (this) {
      HYPHENS -> 0.07f
      ASTERISKS -> -0.11f
      UNDERSCORES -> 0.42f
    }
}
