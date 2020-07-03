package press.widgets

import android.os.Build.VERSION
import android.widget.EdgeEffect
import android.widget.ScrollView
import androidx.annotation.ColorInt
import press.extensions.reflect

object ScrollViewCompat {

  fun setEdgeEffectColor(view: ScrollView, @ColorInt color: Int) {
    if (VERSION.SDK_INT >= 29) {
      view.topEdgeEffectColor = color
      view.bottomEdgeEffectColor = color

    } else {
      val topEdge = reflect(ScrollView::class, "mEdgeGlowTop").get(view) as EdgeEffect
      val bottomEdge = reflect(ScrollView::class, "mEdgeGlowBottom").get(view) as EdgeEffect
      topEdge.color = color
      bottomEdge.color = color
    }
  }
}
