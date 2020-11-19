package press.navigation.transitions

import android.view.View
import android.view.animation.PathInterpolator
import kotlinx.android.parcel.Parcelize
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import me.saket.inboxrecyclerview.page.StandaloneExpandablePageLayout
import me.saket.press.shared.ui.ScreenKey
import press.navigation.DelegatingScreenKey
import press.navigation.navigator
import press.widgets.dp

/**
 * Morphs an incoming [screen] from a FAB in the outgoing
 * screen (and vice versa) through [MorphFromFabScreenTransition].
 *
 * Also wraps the screen inside an [ExpandablePageLayout] for making it
 * pull-collapsible. Doesn't offer expand/collapse transition.
 */
@Parcelize
data class MorphFromFabScreenKey(val screen: ScreenKey) : DelegatingScreenKey(screen) {
  override fun transformDelegateView(view: View): View {
    return StandaloneExpandablePageLayout(view.context).apply {
      addView(view)
      id = view.id
      view.id = View.NO_ID

      applyStyle()
      expandImmediately()
      onPageRelease = { collapseEligible ->
        if (collapseEligible) {
          navigator().goBack()
        }
      }
    }
  }
}
