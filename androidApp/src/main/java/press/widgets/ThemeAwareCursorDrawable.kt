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
import com.jakewharton.rx.replayingShare
import me.saket.press.shared.theme.listenRx
import org.xmlpull.v1.XmlPullParser
import press.PressApp

class ThemeAwareCursorDrawable(private val state: CursorState = CursorState()) : Drawable() {
  private val paint = Paint(ANTI_ALIAS_FLAG)

  init {
    state.palette.subscribe {
      paint.color = it.accentColor
      invalidateSelf()
    }
  }

  override fun inflate(r: Resources, parser: XmlPullParser, attrs: AttributeSet, theme: Theme?) {
    super.inflate(r, parser, attrs, theme)
    state.width = TypedValue.applyDimension(COMPLEX_UNIT_DIP, 2f, r.displayMetrics).toInt()
  }

  override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
    super.setBounds(left, top, left + state.width, bottom)
  }

  override fun draw(canvas: Canvas) {
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

  override fun getOpacity(): Int {
    return PixelFormat.OPAQUE
  }

  override fun getConstantState(): ConstantState? {
    return state
  }

  class CursorState : ConstantState() {
    var width: Int = 0
    val palette = PressApp.component
      .theme()
      .listenRx()
      .replayingShare()

    override fun newDrawable() = ThemeAwareCursorDrawable(this)
    override fun getChangingConfigurations() = 0
  }
}
