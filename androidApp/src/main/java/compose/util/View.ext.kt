package compose.util

import android.os.Build.VERSION.SDK_INT
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import compose.widgets.Attr
import compose.widgets.DisplayUnit

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

fun View.setElevation(value: DisplayUnit) {
  elevation = value.px(context)
}

inline fun fromOreo(block: () -> Unit) {
  if (SDK_INT >= 26) {
    block()
  }
}

fun Toolbar.findTitleView(): TextView {
  if (subtitle != null && subtitle.isNotBlank()) {
    throw UnsupportedOperationException("TODO")
  }
  return children.find { it is TextView } as TextView
}