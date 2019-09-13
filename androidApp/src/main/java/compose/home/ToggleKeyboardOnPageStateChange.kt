package compose.home

import android.widget.EditText
import androidx.core.view.postDelayed
import compose.widgets.hideKeyboard
import compose.widgets.showKeyboard
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks

class ToggleKeyboardOnPageStateChange(
  private val editText: EditText
) : SimplePageStateChangeCallbacks() {

  override fun onPageAboutToExpand(expandAnimDuration: Long) {
    editText.postDelayed(expandAnimDuration / 2) {
      editText.showKeyboard()
    }
  }

  override fun onPageAboutToCollapse(collapseAnimDuration: Long) {
    editText.postDelayed(collapseAnimDuration / 2) {
      editText.hideKeyboard()
    }
  }
}
