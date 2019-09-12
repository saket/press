package compose.home

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
    fab.postDelayed(collapseAnimDuration / 2) {
      fab.show()
    }
  }
}
