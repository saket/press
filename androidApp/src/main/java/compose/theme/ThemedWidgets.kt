package compose.theme

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.EdgeEffect
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import compose.ComposeApp
import compose.util.findTitleView
import compose.util.reflect
import compose.util.textColor
import compose.widgets.dp
import me.saket.compose.R
import me.saket.compose.shared.theme.ThemePalette

fun themePalette() = ComposeApp.component.themePalette()

fun View.themeAware(onThemeChange: (ThemePalette) -> Unit) {
  themePalette().listen(this, onThemeChange)
}

fun themed(view: TextView): TextView = view

fun themed(view: EditText) = view.apply {
  themeAware {
    val cursorDrawableRes = reflect(TextView::class, "mCursorDrawableRes")
    cursorDrawableRes.set(view, R.drawable.tinted_cursor_drawable)
  }
}

fun themed(view: ScrollView) = view.apply {
  themeAware {
    // TODO: tint overscroll.
  }
}

fun themed(view: RecyclerView) = view.apply {
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
    setColorFilter(ColorUtils.blendARGB(it.fabColor, Color.BLACK, 0.65f))
  }
}