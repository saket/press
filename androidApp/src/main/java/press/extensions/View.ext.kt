@file:Suppress("NOTHING_TO_INLINE")

package press.extensions

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build.VERSION.SDK_INT
import android.text.Editable
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewGroup.OnHierarchyChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import me.saket.press.shared.theme.ThemePalette
import me.saket.wysiwyg.widgets.SimpleTextWatcher
import press.widgets.Attr
import java.util.ArrayDeque
import kotlin.DeprecationLevel.ERROR

inline fun View.string(@StringRes stringRes: Int) = resources.getString(stringRes)

@get:Deprecated(message = "Impossible", level = ERROR)
var View.padding: Int
  get() = throw UnsupportedOperationException()
  set(padding) {
    setPadding(padding, padding, padding, padding)
  }

var TextView.textColor: Int
  get() = currentTextColor
  set(color) {
    setTextColor(color)
  }

var TextView.textSizePx: Float
  get() = textSize
  set(size) {
    setTextSize(COMPLEX_UNIT_PX, size)
  }

inline fun fromOreo(crossinline block: () -> Unit) {
  if (SDK_INT >= 26) {
    block()
  }
}

inline fun Toolbar.findTitleView(): TextView {
  if (subtitle != null && subtitle.isNotBlank()) {
    throw UnsupportedOperationException("TODO")
  }
  if (title == null) {
    // Toolbar lazy creates the title TextView.
    title = " "
  }
  return children.find { it is TextView } as TextView
}

fun EditText.showKeyboard(): Boolean {
  requestFocus()
  val inputManager = context.getSystemService(InputMethodManager::class.java)!!
  return inputManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideKeyboard() {
  val inputManager = context.getSystemService(InputMethodManager::class.java)!!
  inputManager.hideSoftInputFromWindow(windowToken, 0)
}

inline fun View.locationOnScreen(): Rect {
  val loc = IntArray(2)
  getLocationOnScreen(loc)
  return Rect(loc[0], loc[1], loc[0] + width, loc[1] + height)
}

inline fun EditText.doOnTextChange(crossinline action: (Editable) -> Unit) {
  addTextChangedListener(object : SimpleTextWatcher {
    override fun afterTextChanged(text: Editable) = action(text)
  })
  action(text)  // initial update.
}

inline fun View.updateMargins(bottom: Int) {
  updateLayoutParams<MarginLayoutParams> {
    bottomMargin = bottom
  }
}

inline fun View.updatePadding(horizontal: Int, vertical: Int) {
  updatePadding(left = horizontal, right = horizontal, top = vertical, bottom = vertical)
}

fun ViewFlipper.setDisplayedChild(child: View) {
  val childIndex = indexOfChild(child)
  if (displayedChild != childIndex) {     // otherwise ViewFlipper plays animation even if the same child is set.
    displayedChild = childIndex
  }
}

fun createRippleDrawable(
  palette: ThemePalette,
  background: Int = Color.TRANSPARENT,
  borderless: Boolean = false
): RippleDrawable {
  val shape = if (borderless) null else PaintDrawable(background)
  val mask = if (borderless) null else PaintDrawable(Color.BLACK)
  return RippleDrawable(ColorStateList.valueOf(palette.buttonPressed), shape, mask)
}

inline fun ViewGroup.onViewAdds(crossinline action: (View) -> Unit) {
  setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
    override fun onChildViewAdded(parent: View, child: View) = action(child)
    override fun onChildViewRemoved(parent: View, child: View) = Unit
  })
}

inline fun <reified T> View.findChild(): T? {
  val queue = ArrayDeque<View>()
  queue.addFirst(this)

  while (true) {
    when (val current = queue.poll()) {
      null -> return null
      is T -> return current
      is ViewGroup -> current.forEach { queue.addLast(it) }
    }
  }
}

inline fun EditText.doOnEditorAction(imeAction: Int, crossinline action: () -> Unit) {
  setOnEditorActionListener { _, actionId, _ ->
    if (actionId == imeAction) {
      action()
      true
    } else {
      false
    }
  }
}

inline fun EditText.setTextAndCursor(newText: String) {
  setText(newText)
  setSelection(this.text.length)  // EditText's input-filters may change the set text.
}
