package compose.util

import android.os.Build.VERSION.SDK_INT
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import compose.widgets.Attr

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

@get:Deprecated(message = "Impossible", level = DeprecationLevel.ERROR)
var TextView.textAppearance: Int
  get() = throw UnsupportedOperationException()
  set(resId) {
    setTextAppearance(resId)
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
