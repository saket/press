package press.theme

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
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils.blendARGB
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.attaches
import com.jakewharton.rxbinding3.view.detaches
import press.App
import press.util.onDestroys
import press.widgets.PorterDuffColorFilterWrapper
import press.widgets.findTitleView
import press.widgets.textColor
import me.saket.press.shared.theme.ThemePalette
import me.saket.press.R
import press.widgets.ScrollViewCompat

fun themePalette() = App.component.themePalette()

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

fun themed(view: TextView): TextView = view.apply {
  typeface = ResourcesCompat.getFont(context, R.font.work_sans_regular)

  themeAware {
    highlightColor = it.textHighlightColor
  }
}

fun themed(view: EditText) = view.apply {
  typeface = ResourcesCompat.getFont(context, R.font.work_sans_regular)
  val selectionHandleDrawables = TextViewCompat.textSelectionHandles(this)

  themeAware { palette ->
    selectionHandleDrawables.forEach { it.setColorFilter(palette.accentColor, SRC_IN) }
    highlightColor = palette.textHighlightColor
  }
}

fun themed(view: ScrollView) = view.apply {
  themeAware {
    ScrollViewCompat.setEdgeEffectColor(view, it.accentColor)
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
  val titleView = findTitleView()
  titleView.typeface = ResourcesCompat.getFont(context, R.font.work_sans_bold)

  themeAware {
    background = ColorDrawable(it.primaryColor)
    titleView.textColor = it.accentColor
  }
}

fun themed(view: FloatingActionButton) = view.apply {
  themeAware {
    backgroundTintList = ColorStateList.valueOf(it.fabColor)
    colorFilter = PorterDuffColorFilterWrapper(blendARGB(it.fabColor, BLACK, 0.65f))
  }
}
