package press.navigation.transitions

import android.view.animation.PathInterpolator
import me.saket.inboxrecyclerview.page.ExpandablePageLayout
import press.widgets.dp

fun ExpandablePageLayout.applyStyle() {
  elevation = dp(40f)
  animationInterpolator = PathInterpolator(0.5f, 0f, 0f, 1f)
  animationDurationMillis = 350
}
