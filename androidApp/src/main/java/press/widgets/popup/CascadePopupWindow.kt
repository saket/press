package press.widgets.popup

import android.R.attr.listChoiceBackgroundIndicator
import android.R.attr.popupBackground
import android.R.attr.popupElevation
import android.R.attr.popupEnterTransition
import android.R.attr.popupExitTransition
import android.content.Context
import android.transition.Transition
import android.transition.TransitionInflater
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
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

  protected val themeAttrs = resolveThemeAttrs()

  init {
    isFocusable = true    // Dismiss on outside touch.
    isOutsideTouchable = true
    elevation = themeAttrs.popupElevation

    setBackgroundDrawable(null)   // Remove PopupWindow's default frame around the content.
    PopupWindowCompat.setOverlapAnchor(this, true)

    enterTransition = themeAttrs.popupEnterTransition
    exitTransition = themeAttrs.popupExitTransition
  }

  private fun resolveThemeAttrs(): ThemeAttributes {
    val attrs = intArrayOf(
        popupBackground,
        popupElevation,
        popupEnterTransition,
        popupExitTransition,
        listChoiceBackgroundIndicator
    )

    return context.obtainStyledAttributes(android.R.style.Widget_Material_PopupMenu, attrs).use {
      val inflateTransition = { resId: Int -> TransitionInflater.from(context).inflateTransition(resId) }
      ThemeAttributes(
          popupBackgroundRes = it.getResourceIdOrThrow(attrs.indexOf(popupBackground)),
          popupElevation = it.getDimensionOrThrow(attrs.indexOf(popupElevation)),
          popupEnterTransition = inflateTransition(it.getResourceIdOrThrow(attrs.indexOf(popupEnterTransition))),
          popupExitTransition = inflateTransition(it.getResourceIdOrThrow(attrs.indexOf(popupExitTransition))),
          touchFeedbackRes = it.getResourceIdOrThrow(attrs.indexOf(listChoiceBackgroundIndicator))
      )
    }
  }

  data class ThemeAttributes(
    @DrawableRes val popupBackgroundRes: Int,
    @Px val popupElevation: Float,
    @DrawableRes val touchFeedbackRes: Int,
    val popupEnterTransition: Transition,
    val popupExitTransition: Transition
  )
}
