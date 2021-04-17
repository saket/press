package press.widgets

import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import org.xmlpull.v1.XmlPullParser
import press.PressApp

class ThemeAwareCursorDrawable : Drawable() {
  private var width: Int = 0
  private val paint = Paint(ANTI_ALIAS_FLAG)

  override fun inflate(r: Resources, parser: XmlPullParser, attrs: AttributeSet, theme: Theme?) {
    super.inflate(r, parser, attrs, theme)
    width = TypedValue.applyDimension(COMPLEX_UNIT_DIP, 2f, r.displayMetrics).toInt()
  }

  override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
    super.setBounds(left, top, left + width, bottom)
  }

  override fun draw(canvas: Canvas) {
    PressApp.component.theme().palette.let {
      if (paint.color != it.accentColor) {
        paint.color = it.accentColor
      }
    }
    canvas.drawRect(bounds, paint)
  }

  override fun setAlpha(alpha: Int) {
    paint.alpha = alpha
    invalidateSelf()
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    paint.colorFilter = colorFilter
    invalidateSelf()
  }

  override fun getOpacity(): Int = PixelFormat.OPAQUE
  override fun getConstantState(): ConstantState = CursorConstantState

  // A constant state isn't needed for this drawable, but some manufacturers crash without one.
  private object CursorConstantState : ConstantState() {
    override fun newDrawable(): Drawable = ThemeAwareCursorDrawable()
    override fun getChangingConfigurations(): Int = 0
  }
}
