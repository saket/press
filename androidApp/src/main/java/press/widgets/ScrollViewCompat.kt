package press.widgets

import android.os.Build.VERSION
import android.widget.EdgeEffect
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.annotation.ColorInt
import press.extensions.reflect

object ScrollViewCompat {
  fun setEdgeEffectColor(view: ScrollView, @ColorInt color: Int) {
    if (VERSION.SDK_INT >= 29) {
      view.setEdgeEffectColor(color)

    } else {
      val topEdge = reflect(ScrollView::class, "mEdgeGlowTop")?.get(view) as? EdgeEffect ?: return
      val bottomEdge = reflect(ScrollView::class, "mEdgeGlowBottom")!!.get(view) as EdgeEffect
      topEdge.color = color
      bottomEdge.color = color
    }
  }

  fun setEdgeEffectColor(view: HorizontalScrollView, @ColorInt color: Int) {
    if (VERSION.SDK_INT >= 29) {
      view.setEdgeEffectColor(color)

    } else {
      val leftEdge = reflect(HorizontalScrollView::class, "mEdgeGlowLeft")?.get(view) as? EdgeEffect ?: return
      val rightEdge = reflect(HorizontalScrollView::class, "mEdgeGlowRight")!!.get(view) as EdgeEffect
      leftEdge.color = color
      rightEdge.color = color
    }
  }
}
