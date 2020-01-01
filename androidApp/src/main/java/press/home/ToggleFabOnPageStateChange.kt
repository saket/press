package press.home

import androidx.core.view.postDelayed
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks

class ToggleFabOnPageStateChange(
  private val fab: FloatingActionButton
) : SimplePageStateChangeCallbacks() {

  override fun onPageAboutToExpand(expandAnimDuration: Long) {
    fab.hide()
  }

  override fun onPageAboutToCollapse(collapseAnimDuration: Long) {
    // Delaying a bit ensures the FAB doesn't show up momentarily
    // above the keyboard while the page is collapsing.
    fab.postDelayed(collapseAnimDuration * 3 / 4) {
      fab.show()
    }
  }
}
