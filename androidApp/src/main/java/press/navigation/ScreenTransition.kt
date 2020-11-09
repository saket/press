package press.navigation

import android.view.View

interface ScreenTransition {
  fun transition(from: View, to: View)
}
