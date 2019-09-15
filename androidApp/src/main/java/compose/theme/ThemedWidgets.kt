package compose.theme

import android.content.res.ColorStateList
import android.graphics.Color.BLACK
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.EdgeEffect
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils.blendARGB
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.attaches
import com.jakewharton.rxbinding3.view.detaches
import compose.ComposeApp
import compose.util.onDestroys
import compose.util.reflect
import compose.util.setOpacity
import compose.widgets.PorterDuffColorFilterWrapper
import compose.widgets.dp
import compose.widgets.findTitleView
import compose.widgets.textColor
import me.saket.compose.shared.theme.ThemePalette

fun themePalette() = ComposeApp.component.themePalette()

fun View.themeAware(onThemeChange: (ThemePalette) -> Unit) {
  attaches()
      .switchMap { themePalette() }
      .takeUntil(detaches())
      .subscribe { onThemeChange(it) }
}

fun AppCompatActivity.themeAware(onThemeChange: (ThemePalette) -> Unit) {
  themePalette()
      .takeUntil(onDestroys())
      .subscribe { onThemeChange(it) }
}

fun themed(view: TextView): TextView = view

fun themed(view: EditText) = view.apply {
  val selectionHandleDrawables = EditTextSelectionHandleReflection.find(this)

  themeAware { palette ->
    selectionHandleDrawables.forEach { it.setColorFilter(palette.accentColor, SRC_IN) }
    highlightColor = palette.accentColor.setOpacity(0.3f)
  }
}

fun themed(view: ScrollView) = view.apply {
  val topEdge = reflect(ScrollView::class, "mEdgeGlowTop").get(view) as EdgeEffect
  val bottomEdge = reflect(ScrollView::class, "mEdgeGlowBottom").get(view) as EdgeEffect

  themeAware {
    topEdge.color = it.accentColor
    bottomEdge.color = it.accentColor
  }
}

fun <T : RecyclerView> themed(view: T) = view.apply {
  themeAware {
    edgeEffectFactory = object : EdgeEffectFactory() {
      override fun createEdgeEffect(view: RecyclerView, direction: Int) =
        EdgeEffect(view.context).apply { color = it.accentColor }
    }
  }
}

fun themed(toolbar: Toolbar) = toolbar.apply {
  themeAware {
    background = ColorDrawable(it.primaryColor)
    elevation = dp(4f)
    findTitleView().textColor = it.accentColor
  }
}

fun themed(view: FloatingActionButton) = view.apply {
  themeAware {
    backgroundTintList = ColorStateList.valueOf(it.fabColor)
    colorFilter = PorterDuffColorFilterWrapper(blendARGB(it.fabColor, BLACK, 0.65f))
  }
}