package press.widgets

import android.graphics.PorterDuff.Mode
import android.graphics.PorterDuffColorFilter

/**
 * Because PorterDuffColorFilter doesn't expose its color.
 */
class PorterDuffColorFilterWrapper(
  val color: Int,
  mode: Mode = Mode.SRC_ATOP
) : PorterDuffColorFilter(color, mode)