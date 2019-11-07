package press.home

import android.view.Window
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks

class ToggleSoftInputModeOnPageStateChange(
  private val window: Window
) : SimplePageStateChangeCallbacks() {

  init {
    window.setSoftInputMode(SOFT_INPUT_ADJUST_NOTHING)
  }

  override fun onPageExpanded() =
    window.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)

  override fun onPageCollapsed() =
    window.setSoftInputMode(SOFT_INPUT_ADJUST_NOTHING)
}
