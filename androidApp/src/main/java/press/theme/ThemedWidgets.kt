package press.theme

import android.content.res.ColorStateList
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Button
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
import io.reactivex.Observable
import me.saket.press.R
import me.saket.press.shared.theme.ThemePalette
import press.PressApp
import press.extensions.findTitleView
import press.extensions.onDestroys
import press.extensions.textColor
import press.widgets.PorterDuffColorFilterWrapper
import press.widgets.PressButton
import press.widgets.ScrollViewCompat

fun themePalette(): Observable<ThemePalette> = PressApp.component.themePalette()

fun View.themeAware(onThemeChange: (ThemePalette) -> Unit) {
  val stream = themePalette()
  attaches()
      .switchMap { stream }
      .takeUntil(detaches())
      .mergeWith(stream.take(1))  // don't wait till attach for the first emission.
      .distinctUntilChanged()
      .subscribe { onThemeChange(it) }
}

fun AppCompatActivity.themeAware(onThemeChange: (ThemePalette) -> Unit) {
  themePalette()
      .takeUntil(onDestroys())
      .subscribe { onThemeChange(it) }
}

fun <T : TextView> themed(view: T): T = view.apply {
  typeface = ResourcesCompat.getFont(context, R.font.work_sans_regular)

  themeAware {
    highlightColor = it.textHighlightColor
  }
}

fun <T : Button> themed(view: T): T = view.apply {
  check(view is PressButton) { "Use PressButton instead" }
  typeface = ResourcesCompat.getFont(context, R.font.work_sans_regular)
}

fun <T : EditText> themed(view: T): T = view.apply {
  require(view !is AppCompatEditText) { "Cursor tinting doesn't work with AppCompatEditText, not sure why." }

  typeface = ResourcesCompat.getFont(context, R.font.work_sans_regular)
  val selectionHandleDrawables = TextViewCompat.textSelectionHandles(this)

  themeAware { palette ->
    selectionHandleDrawables.forEach { it.setColorFilter(palette.accentColor, SRC_IN) }
    highlightColor = palette.textHighlightColor
    setHintTextColor(palette.textColorSecondary)
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
