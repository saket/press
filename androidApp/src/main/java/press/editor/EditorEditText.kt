package press.editor

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff.Mode.SRC_ATOP
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.text.Layout
import android.widget.EditText
import me.saket.press.R
import me.saket.resourceinterceptor.ContextResourceWrapper
import me.saket.resourceinterceptor.DrawableInterceptor
import me.saket.resourceinterceptor.InterceptibleResources
import me.saket.wysiwyg.spans.HeadingSpan

class EditorEditText(context: Context) :
    EditText(ContextResourceWrapper(context, InterceptibleResources(context.resources))) {

  init {
    val resources = resources as InterceptibleResources
    resources.setInterceptor(
        resId = R.drawable.tinted_cursor_drawable,
        interceptor = DrawableInterceptor { systemDrawable ->
          ClippedCursorDrawable(this, systemDrawable().mutate())
        }
    )
  }

  class ClippedCursorDrawable(val view: EditText, delegate: Drawable) : DrawableWrapper(delegate) {
    init {
      setColorFilter(Color.YELLOW, SRC_ATOP)
    }

//    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
//      println("------------------------------------------")
//      println("Bounds: $left, $top, $right, $bottom")
//
//      val lineBottomSpacing = when(val layout = view.layout) {
//        null -> 0
//        else -> lineBottomSpacing(layout, top)
//      }
//
//      println("Updated bounds: $left, $top, $right, $bottom")
//      super.setBounds(left, top, right, bottom - lineBottomSpacing)
//    }
//
//    private fun lineBottomSpacing(layout: Layout, top: Int): Int {
//      val cursorAtLine = layout.getLineForOffset(top)
//      println("cursorAtLine: $cursorAtLine")
//
//      val lineStart = layout.getLineStart(cursorAtLine)
//      val lineEnd = layout.getLineEnd(cursorAtLine)
//      val headingSpan = view.text.getSpans(lineStart, lineEnd, HeadingSpan::class.java).firstOrNull()
//      println("HeadingSpan: $headingSpan")
//
////      return headingSpan?.bottomSpacing(layout.paint.density) ?: 0
//      return 0
//    }
  }
}
