@file:Suppress("NOTHING_TO_INLINE")

package press.extensions

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.RippleDrawable
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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import me.saket.press.shared.theme.withAlpha
import me.saket.wysiwyg.widgets.SimpleTextWatcher
import press.theme.themePalette
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

inline fun View.updatePadding(horizontal: Int) {
  updatePadding(left = horizontal, right = horizontal)
}

fun ViewFlipper.setDisplayedChild(child: View) {
  val childIndex = indexOfChild(child)
  if (displayedChild != childIndex) {     // otherwise ViewFlipper plays animation even if the same child is set.
    displayedChild = childIndex
  }
}

fun View.rippleDrawable(
  color: Int = themePalette().pressedColor(themePalette().accentColor).withAlpha(0.1f),
  background: Int = Color.TRANSPARENT,
): RippleDrawable {
  val shape = PaintDrawable(background)
  val mask = PaintDrawable(Color.BLACK)
  return RippleDrawable(ColorStateList.valueOf(color), shape, mask)
}

fun View.borderlessRippleDrawable(
  color: Int = themePalette().pressedColor(themePalette().accentColor).withAlpha(0.25f)
): RippleDrawable {
  return RippleDrawable(ColorStateList.valueOf(color), null, null)
}

inline fun ViewGroup.onViewAdds(crossinline action: (View) -> Unit) {
  setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
    override fun onChildViewAdded(parent: View, child: View) = action(child)
    override fun onChildViewRemoved(parent: View, child: View) = Unit
  })
}

inline fun <reified T> View.findParent(): T {
  var parent = this.parent
  while (parent !is T) {
    parent = parent.parent
    if (parent !is ViewGroup) {
      error("Couldn't find ${T::class.simpleName}")
    }
  }
  return parent
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

inline fun TextView.setDrawableLeft(@DrawableRes drawableRes: Int) {
  setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0)
}

inline fun View.doOnEveryLayout(crossinline action: () -> Unit) {
  addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
    action()
  }
}
