package press.navigation

import android.content.Context
import android.graphics.Color.BLACK
import android.view.View
import android.view.animation.PathInterpolator
import me.saket.inboxrecyclerview.dimming.AnimatedVisibilityColorDrawable
import me.saket.inboxrecyclerview.page.StandaloneExpandablePageLayout
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.ui.ScreenKey
import press.navigation.transitions.ExpandableScreenTransition
import press.navigation.transitions.MorphFromFabScreenTransition
import press.theme.themeAware
import kotlin.math.roundToInt

/**
 * Creates and optionally transforms screens depending
 * upon the device factory (phones vs tablets vs desktop).
 */
interface FormFactor {
  fun createView(context: Context, screen: ScreenKey): View

  // Hack hack :/
  fun findDecoratedScreenView(view: View): View
}

/**
 * Shows all screens as pull-collapsible.
 */
class PhoneFormFactor(private val viewFactories: ViewFactories) : FormFactor {
  override fun createView(context: Context, screen: ScreenKey): View {
    val view = viewFactories.createView(context, screen).let {
      if (HomeScreenKey.isRoot(screen) || it is NotPullCollapsible) {
        it
      } else {
        makeScreenPullCollapsible(it)
      }
    }
    setDimmingForeground(view)
    maybeSetThemeBackground(view)
    return view
  }

  override fun findDecoratedScreenView(view: View): View {
    return when (view) {
      is StandaloneExpandablePageLayout -> view.getChildAt(0)
      else -> view
    }
  }

  private fun makeScreenPullCollapsible(view: View): View {
    return StandaloneExpandablePageLayout(view.context).apply {
      check(view.id != View.NO_ID)
      addView(view)
      id = view.id
      view.id = View.NO_ID

      animationInterpolator = PathInterpolator(0.5f, 0f, 0f, 1f)
      animationDurationMillis = SCREEN_TRANSITION_DURATION
      contentOpacityWhenCollapsed = 0f

      onPageRelease = { collapseEligible ->
        if (collapseEligible) {
          navigator().goBack()
        }
      }
    }
  }

  /**
   * This dimming drawable is smoothly animated during screen transitions.
   * See [ExpandableScreenTransition] and [MorphFromFabScreenTransition].
   */
  private fun setDimmingForeground(view: View) {
    view.foreground = AnimatedVisibilityColorDrawable(
      color = BLACK,
      maxAlpha = (0.25f * 255).roundToInt(),
      animDuration = SCREEN_TRANSITION_DURATION
    )
  }

  private fun maybeSetThemeBackground(view: View) {
    if (view.background == null) {
      view.themeAware {
        view.setBackgroundColor(it.window.backgroundColor)
      }
    }
  }

  companion object {
    const val SCREEN_TRANSITION_DURATION = 350L
  }
}
