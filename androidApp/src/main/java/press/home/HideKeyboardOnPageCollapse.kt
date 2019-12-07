package press.home

import android.widget.EditText
import androidx.core.view.postDelayed
import press.widgets.hideKeyboard
import press.widgets.showKeyboard
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks

class ToggleKeyboardOnPageStateChange(
  private val editText: EditText
) : SimplePageStateChangeCallbacks() {

  override fun onPageAboutToExpand(expandAnimDuration: Long) {
    editText.showKeyboard()
  }

  override fun onPageAboutToCollapse(collapseAnimDuration: Long) {
    editText.postDelayed(collapseAnimDuration / 2) {
      editText.hideKeyboard()
    }
  }
}
