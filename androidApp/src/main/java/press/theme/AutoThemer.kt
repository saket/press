package press.theme

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.PorterDuff.Mode.SRC_ATOP
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.view.ViewGroup
import android.view.Window.ID_ANDROID_CONTENT
import android.widget.Button
import android.widget.EdgeEffect
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.EdgeEffectFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.saket.press.R
import press.extensions.createRippleDrawable
import press.extensions.findTitleView
import press.extensions.onViewAdds
import press.extensions.textColor
import press.widgets.PressButton
import press.widgets.ScrollViewCompat
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
      is Button -> themed(view)
      is TextView -> themed(view)
      is ScrollView -> themed(view)
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

  themeAware { palette ->
    highlightColor = palette.textHighlightColor
    selectionHandleDrawables.forEach { it.setColorFilter(palette.accentColor, SRC_IN) }
    setLinkTextColor(palette.accentColor)
  }
}

private fun <T : Button> themed(view: T): T = view.apply {
  check(view is PressButton) { "Use PressButton instead" }
}

private fun <T : EditText> themed(view: T): T = view.apply {
  themed(view as TextView)

  themeAware { palette ->
    setHintTextColor(palette.textColorSecondary)
  }
}

private fun themed(view: ScrollView) = view.apply {
  themeAware {
    ScrollViewCompat.setEdgeEffectColor(view, it.accentColor)
  }
}

private fun themed(view: HorizontalScrollView) = view.apply {
  themeAware {
    ScrollViewCompat.setEdgeEffectColor(view, it.accentColor)
  }
}

private fun <T : RecyclerView> themed(view: T): T = view.apply {
  themeAware {
    edgeEffectFactory = object : EdgeEffectFactory() {
      override fun createEdgeEffect(view: RecyclerView, direction: Int) =
        EdgeEffect(view.context).apply { color = it.accentColor }
    }
  }
}

private fun themed(toolbar: Toolbar) = toolbar.apply {
  val titleView = findTitleView()
  titleView.typeface = ResourcesCompat.getFont(context, R.font.work_sans_bold)

  themeAware {
    titleView.textColor = it.textColorHeading
  }

  val setRipple = { child: View ->
    child.themeAware {
      child.background = createRippleDrawable(it, borderless = true).apply {
        radius = maxOf(radius, context.dp(20))
      }
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
  themeAware {
    backgroundTintList = ColorStateList.valueOf(it.fabColor)
    colorFilter = PorterDuffColorFilter(it.fabIcon, SRC_ATOP)
  }
}

private fun <T : ProgressBar> themed(view: T): T = view.apply {
  themeAware {
    view.indeterminateTintList = ColorStateList.valueOf(it.accentColor)
  }
}
