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
  override fun createView(context: Context, screen: ScreenKey): View {
    val view = viewFactories.createView(context, screen).let {
      when {
        HomeScreenKey.isRoot(screen) -> it
        else -> makeScreenPullCollapsible(it)
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

      elevation = dp(40f)
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
