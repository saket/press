@file:Suppress("NOTHING_TO_INLINE")

package press.extensions

import android.graphics.Rect
import android.os.Build.VERSION.SDK_INT
import android.text.Editable
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewAnimator
import android.widget.ViewFlipper
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import me.saket.wysiwyg.widgets.SimpleTextWatcher
import press.widgets.Attr
import kotlin.DeprecationLevel.ERROR

inline fun View.string(@StringRes stringRes: Int) = resources.getString(stringRes)

inline fun View.attr(@AttrRes resId: Int) = Attr(resId, context)

@get:Deprecated(message = "Impossible", level = ERROR)
var EditText.hintRes: Int
  get() = throw UnsupportedOperationException()
  set(resId) {
    hint = string(resId)
  }

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
}

val View.parentView: ViewGroup get() = parent as ViewGroup

inline fun View.doOnAttach(crossinline action: () -> Unit) {
  if (isAttachedToWindow) {
    action()
  }
  addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
    override fun onViewDetachedFromWindow(v: View) = Unit
    override fun onViewAttachedToWindow(v: View) =
      removeOnAttachStateChangeListener(this).also {
        action()
      }
  })
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
