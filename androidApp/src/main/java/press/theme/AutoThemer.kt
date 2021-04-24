package press.theme

import android.R.attr.state_checked
import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color.WHITE
import android.graphics.PorterDuff.Mode.SRC_ATOP
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.view.ViewGroup
import android.view.Window.ID_ANDROID_CONTENT
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EdgeEffect
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import me.saket.press.R
import me.saket.press.shared.theme.TextStyles.appTitle
import me.saket.press.shared.theme.applyStyle
import me.saket.press.shared.theme.blendWith
import me.saket.press.shared.theme.withAlpha
import press.extensions.borderlessRippleDrawable
import press.extensions.findTitleView
import press.extensions.onViewAdds
import press.extensions.textColor
import press.widgets.PressButton
import press.widgets.ScrollViewCompat
import press.widgets.colorStateListOf
import press.widgets.dp

/**
 * Registers theme change listeners on each Views of a View hierarchy so
 * that the app's theme can change without requiring a restart.
 */
object AutoThemer {
  fun theme(activity: Activity) {
    themeGroup(activity.findViewById(ID_ANDROID_CONTENT))
  }

  fun themeGroup(viewGroup: ViewGroup) {
    viewGroup.onViewAdds { child ->
      when (child) {
        is ViewGroup -> themeGroup(child)
        else -> themeView(child)
      }
    }

    themeView(viewGroup)
    viewGroup.children.forEach { child ->
      when (child) {
        is ViewGroup -> themeGroup(child)
        else -> themeView(child)
      }
    }
  }

  private fun themeView(view: View) {
    // Views can get recycled. Avoid theming them again.
    if (view.getTag(R.id.theming_done) as? Boolean == true) return
    view.setTag(R.id.theming_done, true)

    when (view) {
      is EditText -> themed(view)
      is CheckBox -> themed(view)
      is SwitchMaterial -> themed(view)
      is Button -> themed(view)
      is TextView -> themed(view)
      is ScrollView -> themed(view)
      is NestedScrollView -> themed(view)
      is HorizontalScrollView -> themed(view)
      is RecyclerView -> themed(view)
      is Toolbar -> themed(view)
      is FloatingActionButton -> themed(view)
      is ProgressBar -> themed(view)
    }
  }
}

private fun <T : TextView> themed(view: T): T = view.apply {
  val selectionHandleDrawables = TextViewCompat.textSelectionHandles(this)

  highlightColor = themePalette().textColorHighlight
  selectionHandleDrawables.forEach { it.setColorFilter(themePalette().accentColor, SRC_IN) }
  setLinkTextColor(themePalette().accentColor)
}

private fun <T : Button> themed(view: T): T = view.apply {
  check(view is PressButton || view is CompoundButton) { "Use PressButton instead" }
}

private fun <T : EditText> themed(view: T): T = view.apply {
  themed(view as TextView)

  setHintTextColor(themePalette().textColorSecondary)
}

private fun <T : CheckBox> themed(view: T): T = view.apply {
  background = borderlessRippleDrawable()
  buttonTintList = ColorStateList.valueOf(themePalette().accentColor)
}

private fun <T : SwitchMaterial> themed(view: T): T = view.apply {
  background = borderlessRippleDrawable()
  thumbTintList = colorStateListOf(
    intArrayOf(state_checked) to themePalette().accentColor,
    intArrayOf(-state_checked) to themePalette().window.backgroundColor.blendWith(WHITE, 0.7f)
  )
  trackTintList = colorStateListOf(
    intArrayOf(state_checked) to themePalette().accentColor.withAlpha(0.5f),
    intArrayOf(-state_checked) to themePalette().window.backgroundColor.blendWith(WHITE, 0.2f)
  )
}

private fun themed(view: ScrollView) = view.apply {
  ScrollViewCompat.setEdgeEffectColor(view, themePalette().accentColor)
}

private fun themed(view: NestedScrollView) = view.apply {
  ScrollViewCompat.setEdgeEffectColor(view, themePalette().accentColor)
}

private fun themed(view: HorizontalScrollView) = view.apply {
  ScrollViewCompat.setEdgeEffectColor(view, themePalette().accentColor)
}

private fun <T : RecyclerView> themed(view: T): T = view.apply {
  edgeEffectFactory = object : EdgeEffectFactory() {
    override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
      return EdgeEffect(view.context).apply { color = themePalette().accentColor }
    }
  }
}

private fun themed(toolbar: Toolbar) = toolbar.apply {
  val titleView = findTitleView()
  titleView.applyStyle(appTitle)
  titleView.textColor = themePalette().textColorHeading

  val setRipple = { child: View ->
    child.background = borderlessRippleDrawable().apply {
      radius = maxOf(radius, context.dp(20))
    }
  }

  toolbar.viewChanges { child ->
    if (child is ImageButton) {
      // Navigation icon.
      setRipple(child)
    }
    if (child is ActionMenuView) {
      // Menu items.
      child.viewChanges(setRipple)
    }
  }
}

private fun ViewGroup.viewChanges(action: (View) -> Unit) {
  children.forEach(action)
  onViewAdds(action)
}

private fun themed(view: FloatingActionButton) = view.apply {
  val palette = themePalette()
  backgroundTintList = ColorStateList.valueOf(palette.fabColor)
  colorFilter = PorterDuffColorFilter(palette.fabIcon, SRC_ATOP)
  rippleColor = palette.fabColorPressed
}

private fun <T : ProgressBar> themed(view: T): T = view.apply {
  view.indeterminateTintList = ColorStateList.valueOf(themePalette().accentColor)
}
