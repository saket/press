package press.editor

import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.ime

/**
 * Scrolls to the line under the cursor when the soft keyboard is shown.
 */
class KeepCursorVisibleOnKeyboardShow(
  private val scrollView: EditorScrollView,
  private val editText: EditText
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {

  override fun onEnd(animation: WindowInsetsAnimationCompat) {
    if (ViewCompat.getRootWindowInsets(scrollView)?.isKeyboardVisible == false) return
    if (editText.selectionStart != editText.selectionEnd) return  // There is no cursor. Some text must be selected.

    // An advantage of having a long fading edge is that ScrollView will ensure that
    // there's always the same gap present between the cursor line and the keyboard.
    // If this wasn't true then Press would have to manually calculate, say, the
    // n+2nd line to scroll to.
    require(scrollView.verticalFadingEdgeLength > 0)

    scrollView.setScrollAnimationProperties(
      duration = animation.durationMillis,
      interpolator = animation.interpolator!!
    )
    scrollView.post {
      editText.bringPointIntoView(editText.selectionEnd)
    }
  }

  override fun onProgress(
    insets: WindowInsetsCompat,
    runningAnimations: MutableList<WindowInsetsAnimationCompat>
  ) = insets

  private val WindowInsetsCompat.isKeyboardVisible: Boolean get() = isVisible(ime())
}
