package compose.widgets

import android.graphics.drawable.Drawable
import android.os.Build.VERSION.SDK_INT
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children

fun View.string(@StringRes stringRes: Int) = resources.getString(stringRes)

fun View.attr(@AttrRes resId: Int) = Attr(resId, context)

@get:Deprecated(message = "Impossible", level = DeprecationLevel.ERROR)
var EditText.hintRes: Int
  get() = throw UnsupportedOperationException()
  set(resId) {
    hint = string(resId)
  }

@get:Deprecated(message = "Impossible", level = DeprecationLevel.ERROR)
var EditText.padding: Int
  get() = throw UnsupportedOperationException()
  set(padding) {
    setPadding(padding, padding, padding, padding)
  }

var TextView.textColor: Int
  get() = currentTextColor
  set(color) {
    setTextColor(color)
  }

inline fun fromOreo(block: () -> Unit) {
  if (SDK_INT >= 26) {
    block()
  }
}

fun Drawable.mutateAndTint(color: Int): Drawable {
  return mutate().apply { setTint(color) }
}

fun Toolbar.findTitleView(): TextView {
  if (subtitle != null && subtitle.isNotBlank()) {
    throw UnsupportedOperationException("TODO")
  }
  if (title == null) {
    // Toolbar lazy creates the title TextView.
    title = " "
  }
  return children.find { it is TextView } as TextView
}

fun EditText.setTextAndCursor(text: CharSequence) {
  setText(text)
  setSelection(this.text.length)
}

fun View.setBackground(attr: Attr) {
  background = attr.asDrawable()
}