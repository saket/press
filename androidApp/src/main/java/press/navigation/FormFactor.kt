package press.navigation

import android.content.Context
import android.view.View
import android.view.animation.PathInterpolator
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.SimpleOnPullListener
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

class PhoneFormFactor(private val viewFactories: ViewFactories) : FormFactor {
  override fun createView(context: Context, screen: ScreenKey): View {
    val view = viewFactories.createView(context, screen).let {
      if (screen !is HomeScreenKey) {
        makeScreenPullCollapsible(it)
      } else {
        it
      }
    }
    maybeSetThemeBackground(view)
    return view
  }

  private fun makeScreenPullCollapsible(view: View): View {
    return ExpandablePageLayout(view.context).apply {
      check(view.id != View.NO_ID)
      addView(view)
      id = view.id
      view.id = View.NO_ID

      elevation = dp(40f)
      animationInterpolator = PathInterpolator(0.5f, 0f, 0f, 1f)
      animationDurationMillis = 350

      onNextPullToCollapse {
        navigator().goBack()
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

  private inline fun ExpandablePageLayout.onNextPullToCollapse(
    crossinline block: (ExpandablePageLayout) -> Unit
  ) {
    val page = this
    addOnPullListener(object : SimpleOnPullListener() {
      override fun onRelease(collapseEligible: Boolean) {
        if (collapseEligible) {
          block(page)
          removeOnPullListener(this)
        }
      }
    })
  }
}
