package press.widgets.popup

import android.R.attr.listChoiceBackgroundIndicator
import android.R.attr.popupBackground
import android.R.attr.popupElevation
import android.R.attr.popupEnterTransition
import android.R.attr.popupExitTransition
import android.content.Context
import android.transition.TransitionInflater
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.widget.PopupWindowCompat

/**
 * Mimics [PopupMenu] by,
 * - offering the same entry & exit transitions
 * - dismissing on outside tap
 * - setting a default elevation
 */
@Suppress("LeakingThis")
abstract class CascadePopupWindow(
  private val context: Context
) : PopupWindow(context, null) {

  protected val Int.dip: Int
    get() {
      val metrics = context.resources.displayMetrics
      return TypedValue.applyDimension(COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
    }

  protected val themeAttrs = ThemeAttributes(context)

  init {
    isFocusable = true    // Dismiss on outside touch.
    isOutsideTouchable = true
    elevation = themeAttrs.popupElevation()

    setBackgroundDrawable(null)   // Remove PopupWindow's default frame around the content.
    PopupWindowCompat.setOverlapAnchor(this, true)

    enterTransition = themeAttrs.popupEnterTransition()
    exitTransition = themeAttrs.popupExitTransition()
  }

  class ThemeAttributes(private val context: Context) {
    private val attrs = intArrayOf(
        popupBackground,
        popupElevation,
        popupEnterTransition,
        popupExitTransition,
        listChoiceBackgroundIndicator
    )
    private val styledAttrs = context.obtainStyledAttributes(android.R.style.Widget_Material_PopupMenu, attrs)

    fun popupBackground() =
      styledAttrs.getDrawableOrThrow(attrs.indexOf(popupBackground))

    fun popupElevation() =
      styledAttrs.getDimensionOrThrow(attrs.indexOf(popupElevation))

    fun touchFeedback() =
      styledAttrs.getDrawableOrThrow(attrs.indexOf(listChoiceBackgroundIndicator))

    fun popupEnterTransition() =
      with(TransitionInflater.from(context)) {
        inflateTransition(styledAttrs.getResourceIdOrThrow(attrs.indexOf(popupEnterTransition)))
      }

    fun popupExitTransition() =
      with(TransitionInflater.from(context)) {
        inflateTransition(styledAttrs.getResourceIdOrThrow(attrs.indexOf(popupExitTransition)))
      }
  }
}
