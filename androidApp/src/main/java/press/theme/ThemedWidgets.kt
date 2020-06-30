package press.theme

import android.content.res.ColorStateList
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.EdgeEffect
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding3.view.attaches
import com.jakewharton.rxbinding3.view.detaches
import press.PressApp
import press.util.onDestroys
import press.widgets.PorterDuffColorFilterWrapper
import press.widgets.findTitleView
import press.widgets.textColor
import me.saket.press.shared.theme.ThemePalette
import me.saket.press.R
import press.widgets.ScrollViewCompat

fun themePalette() = PressApp.component.themePalette()

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

fun <T: TextView> themed(view: T): T = view.apply {
  typeface = ResourcesCompat.getFont(context, R.font.work_sans_regular)

  themeAware {
    highlightColor = it.textHighlightColor
  }
}

fun <T: EditText> themed(view: T): T = view.apply {
  require(view !is AppCompatEditText) { "Cursor tinting doesn't work with AppCompatEditText, not sure why." }

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

fun <T : RecyclerView> themed(view: T): T = view.apply {
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
    titleView.textColor = it.textColorHeading
  }
}

fun themed(view: FloatingActionButton) = view.apply {
  themeAware {
    backgroundTintList = ColorStateList.valueOf(it.fabColor)
    colorFilter = PorterDuffColorFilterWrapper(it.fabIcon)
  }
}

fun <T : ProgressBar> themed(view: T): T = view.apply {
  themeAware {
    view.indeterminateTintList = ColorStateList.valueOf(it.accentColor)
  }
}
