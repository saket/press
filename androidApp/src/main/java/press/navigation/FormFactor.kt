package press.navigation

import android.content.Context
import android.view.View
import android.view.animation.PathInterpolator
import me.saket.inboxrecyclerview.page.StandaloneExpandablePageLayout
import me.saket.press.shared.home.HomeScreenKey
import me.saket.press.shared.ui.ScreenKey
import press.theme.themeAware
import press.widgets.dp

/**
 * Creates and optionally transforms screens depending
 * upon the device factory (phones vs tablets vs desktop).
 */
interface FormFactor {
  fun createView(context: Context, screen: ScreenKey): View
}

/**
 * Shows all screens as pull-collapsible.
 */
class PhoneFormFactor(private val viewFactories: ViewFactories) : FormFactor {
  companion object {
    const val SCREEN_ELEVATION_DP = 40f
  }

  override fun createView(context: Context, screen: ScreenKey): View {
    val view = viewFactories.createView(context, screen).let {
      if (HomeScreenKey.isRoot(screen) || it is NotPullCollapsible) {
        it.apply { elevation = dp(SCREEN_ELEVATION_DP) }
      } else {
        makeScreenPullCollapsible(it)
      }
    }
    maybeSetThemeBackground(view)
    return view
  }

  private fun makeScreenPullCollapsible(view: View): View {
    return StandaloneExpandablePageLayout(view.context).apply {
      check(view.id != View.NO_ID)
      addView(view)
      id = view.id
      view.id = View.NO_ID

      elevation = dp(SCREEN_ELEVATION_DP)
      animationInterpolator = PathInterpolator(0.5f, 0f, 0f, 1f)
      animationDurationMillis = 350

      onPageRelease = { collapseEligible ->
        if (collapseEligible) {
          navigator().goBack()
        }
      }
    }
  }

  private fun maybeSetThemeBackground(view: View) {
    if (view.background == null) {
      view.themeAware {
        view.setBackgroundColor(it.window.backgroundColor)
      }
    }
  }
}
